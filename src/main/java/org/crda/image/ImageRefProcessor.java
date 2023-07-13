/**
 * Some contents in this file are translated from
 * https://github.com/regclient/regclient/blob/20c847eb49514cfd7bc1958f165e662c33e3f844/types/ref/ref.go
 */

package org.crda.image;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.tooling.model.Strings;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.crda.Constants.imageHeader;

public class ImageRefProcessor implements Processor {

    private static final String patternRegex = "^(?<scheme>([a-z]+))://(?<ref>(.+))$";
    private static final Pattern schemePattern = Pattern.compile(patternRegex);

    private static final String hostRegex = "(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)";
    private static final String hostPortRegex = "(?:" + hostRegex + "(?:" + Pattern.quote(".") + hostRegex + ")*" +
            Pattern.quote(".") + "?" + Pattern.quote(":") + "[0-9]+)";
    private static final String hostDomainRegex = "(?:" + hostRegex + "(?:(?:" + Pattern.quote(".") + hostRegex + ")+" +
            Pattern.quote(".") + "?|" + Pattern.quote(".") + "))";
    private static final String hostUpperRegex = "(?:[a-zA-Z0-9]*[A-Z][a-zA-Z0-9-]*[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[A-Z][a-zA-Z0-9]*)";
    private static final String registryRegex = "(?:" + hostDomainRegex + "|" + hostPortRegex + "|" + hostUpperRegex +
            "|localhost(?:" + Pattern.quote(":") + "[0-9]+))";
    private static final String repoRegex = "[a-z0-9]+(?:(?:[_.-]|__)[a-z0-9]+)*";
    private static final String tagRegex = "[\\w][\\w.-]{0,127}";
    private static final String digestRegex = "[A-Za-z][A-Za-z0-9]*(?:[-_+.][A-Za-z][A-Za-z0-9]*)*[:][\\p{XDigit}]{32,}";
    private static final String registryGroup = "registry";
    private static final String repositoryGroup = "repository";
    private static final String tagGroup = "tag";
    private static final String digestGroup = "digest";
    private static final String refRegex = "^(?:(?<" + registryGroup + ">(" + registryRegex + "))" + Pattern.quote("/") + ")?" +
            "(?<" + repositoryGroup + ">(" + repoRegex + "(?:" + Pattern.quote("/") + repoRegex + ")*))" +
            "(?:" + Pattern.quote(":") + "(?<" + tagGroup + ">(" + tagRegex + ")))?" +
            "(?:" + Pattern.quote("@") + "(?<" + digestGroup + ">(" + digestRegex + ")))?$";
    private static final Pattern refPattern = Pattern.compile(refRegex);

    private static final String dockerRegistryLegacy = "index.docker.io";
    private static final String dockerRegistryDNS = "registry-1.docker.io";
    private static final String dockerLibrary = "library";
    private static final String dockerRegistry = "docker.io";

    @Override
    public void process(Exchange exchange) throws Exception {
        String imageStr = exchange.getIn().getHeader(imageHeader, String.class);

        Matcher schemeMatcher = schemePattern.matcher(imageStr);
        if (schemeMatcher.find() && !Strings.isNullOrEmpty(schemeMatcher.group("scheme"))) {
            throw new IllegalArgumentException(String.format("Format of image reference %s is not supported", imageStr));
        }

        Matcher refMatcher = refPattern.matcher(imageStr);
        if (!refMatcher.find()) {
            if (refPattern.matcher(imageStr.toLowerCase()).find()) {
                throw new IllegalArgumentException(String.format("Image reference %s must be lowercase", imageStr));
            }
            throw new IllegalArgumentException(String.format("Image reference %s is not valid", imageStr));
        }

        String registry = refMatcher.group(registryGroup);
        String repository = refMatcher.group(repositoryGroup);
        String tag = refMatcher.group(tagGroup);
        String digest = refMatcher.group(digestGroup);

        if (Strings.isNullOrEmpty(repository)) {
            throw new IllegalArgumentException(String.format("Image reference %s is not valid", imageStr));
        }

        String[] repoParts = repository.split("/");
        if (Strings.isNullOrEmpty(registry) && "localhost".equals(repoParts[0]) && repoParts.length > 1) {
            registry = repoParts[0];
            repository = String.join("/", Arrays.copyOfRange(repoParts, 1, repoParts.length));
        }

        if (Strings.isNullOrEmpty(registry) || dockerRegistryDNS.equals(registry) || dockerRegistryLegacy.equals(registry)) {
            registry = dockerRegistry;
        }

        if (dockerRegistry.equals(registry) && !repository.contains("/")) {
            repository = dockerLibrary + "/" + repository;
        }

        if (Strings.isNullOrEmpty(tag) && Strings.isNullOrEmpty(digest)) {
            tag = "latest";
        }

        if (Strings.isNullOrEmpty(repository)) {
            throw new IllegalArgumentException(String.format("Image reference %s is not valid", imageStr));
        }
    }
}
