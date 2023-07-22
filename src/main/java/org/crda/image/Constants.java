package org.crda.image;

import io.quarkus.arc.impl.Sets;
import org.crda.image.manifest.model.raw.Platform;

import java.util.Set;

public interface Constants {
    String imageHeader = "image";
    String imageRegRepoHeader = "imageRegRepo";
    String imageRefHeader = "imageRef";
    String platformHeader = "platform";
    String digestsHeader = "digests";
    String digestHeader = "digest";
    String imagePathHeader = "imagePath";

    Set<Platform> supportedPlatforms = Sets.of(
            new Platform().os("aix").arch("ppc64"),
            new Platform().os("android").arch("386"),
            new Platform().os("android").arch("amd64"),
            new Platform().os("android").arch("arm"),
            new Platform().os("android").arch("arm64"),
            new Platform().os("darwin").arch("amd64"),
            new Platform().os("darwin").arch("arm64"),
            new Platform().os("dragonfly").arch("amd64"),
            new Platform().os("freebsd").arch("386"),
            new Platform().os("freebsd").arch("amd64"),
            new Platform().os("freebsd").arch("arm"),
            new Platform().os("illumos").arch("amd64"),
            new Platform().os("ios").arch("arm64"),
            new Platform().os("js").arch("wasm"),
            new Platform().os("linux").arch("386"),
            new Platform().os("linux").arch("amd64"),
            new Platform().os("linux").arch("arm"),
            new Platform().os("linux").arch("arm64"),
            new Platform().os("linux").arch("loong64"),
            new Platform().os("linux").arch("mips"),
            new Platform().os("linux").arch("mipsle"),
            new Platform().os("linux").arch("mips64"),
            new Platform().os("linux").arch("mips64le"),
            new Platform().os("linux").arch("ppc64"),
            new Platform().os("linux").arch("ppc64le"),
            new Platform().os("linux").arch("riscv64"),
            new Platform().os("linux").arch("s390x"),
            new Platform().os("netbsd").arch("386"),
            new Platform().os("netbsd").arch("amd64"),
            new Platform().os("netbsd").arch("arm"),
            new Platform().os("openbsd").arch("386"),
            new Platform().os("openbsd").arch("amd64"),
            new Platform().os("openbsd").arch("arm"),
            new Platform().os("openbsd").arch("arm64"),
            new Platform().os("plan9").arch("386"),
            new Platform().os("plan9").arch("amd64"),
            new Platform().os("plan9").arch("arm"),
            new Platform().os("solaris").arch("amd64"),
            new Platform().os("wasip1").arch("wasm"),
            new Platform().os("windows").arch("386"),
            new Platform().os("windows").arch("amd64"),
            new Platform().os("windows").arch("arm"),
            new Platform().os("windows").arch("arm64")
    );
}
