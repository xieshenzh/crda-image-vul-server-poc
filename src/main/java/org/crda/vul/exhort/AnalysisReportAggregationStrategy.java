package org.crda.vul.exhort;

import com.redhat.exhort.api.v4.*;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterForReflection
public class AnalysisReportAggregationStrategy {
    public AnalysisReport aggregate(AnalysisReport aggregated, AnalysisReport report) {
        if (aggregated == null) {
            aggregated = new AnalysisReport()
                    .scanned(new Scanned())
                    .providers(new HashMap<>());
        }

        Scanned reportScanned = report.getScanned();
        if (reportScanned != null) {
            Scanned aggScanned = aggregated.getScanned();
            aggScanned.total((aggScanned.getTotal() != null ? aggScanned.getTotal() : 0)
                            + (reportScanned.getTotal() != null ? reportScanned.getTotal() : 0))
                    .direct((aggScanned.getDirect() != null ? aggScanned.getDirect() : 0)
                            + (reportScanned.getDirect() != null ? reportScanned.getDirect() : 0))
                    .transitive((aggScanned.getTransitive() != null ? aggScanned.getTransitive() : 0)
                            + (reportScanned.getTransitive() != null ? reportScanned.getTransitive() : 0));
        }

        Map<String, ProviderReport> aggProviders = aggregated.getProviders();
        Map<String, ProviderReport> reportProviders = report.getProviders();
        if (reportProviders != null) {
            reportProviders.forEach((key, value) -> {
                ProviderReport pr = aggProviders.get(key);

                if (pr != null) {
                    ProviderStatus reportStatus = value.getStatus();
                    ProviderStatus aggStatus = pr.getStatus();

                    if (aggStatus != null) {
                        if (reportStatus != null) {
                            if (aggStatus.getName() == null) {
                                if (reportStatus.getName() != null) {
                                    aggStatus.setName(reportStatus.getName());
                                }
                            }
                            aggStatus.setOk((aggStatus.getOk() != null ? aggStatus.getOk() : Boolean.TRUE)
                                    && (reportStatus.getOk() != null ? reportStatus.getOk() : Boolean.TRUE));
                            if (aggStatus.getCode() == null || aggStatus.getCode() == 200) {
                                if (reportStatus.getCode() != null) {
                                    aggStatus.setCode(reportStatus.getCode());
                                }
                            }
                            if (aggStatus.getMessage() == null || "OK".equalsIgnoreCase(aggStatus.getMessage())) {
                                if (reportStatus.getMessage() != null) {
                                    aggStatus.setMessage(reportStatus.getMessage());
                                }
                            }
                        }
                    } else {
                        pr.setStatus(reportStatus);
                    }

                    Map<String, Source> reportSources = value.getSources();
                    Map<String, Source> aggSources = pr.getSources();

                    if (reportSources != null) {
                        reportSources.forEach((sKey, sValue) -> {
                            Source aggSource = aggSources.get(sKey);

                            if (aggSource != null) {
                                SourceSummary reportSummary = sValue.getSummary();
                                SourceSummary aggSummary = aggSource.getSummary();

                                if (aggSummary != null) {
                                    if (reportSummary != null) {
                                        aggSummary.direct((aggSummary.getDirect() != null ? aggSummary.getDirect() : 0)
                                                        + (reportSummary.getDirect() != null ? reportSummary.getDirect() : 0))
                                                .transitive((aggSummary.getTransitive() != null ? aggSummary.getTransitive() : 0)
                                                        + (reportSummary.getTransitive() != null ? reportSummary.getTransitive() : 0))
                                                .total((aggSummary.getTotal() != null ? aggSummary.getTotal() : 0)
                                                        + (reportSummary.getTotal() != null ? reportSummary.getTotal() : 0))
                                                .dependencies((aggSummary.getDependencies() != null ? aggSummary.getDependencies() : 0)
                                                        + (reportSummary.getDependencies() != null ? reportSummary.getDependencies() : 0))
                                                .critical((aggSummary.getCritical() != null ? aggSummary.getCritical() : 0)
                                                        + (reportSummary.getCritical() != null ? reportSummary.getCritical() : 0))
                                                .high((aggSummary.getHigh() != null ? aggSummary.getHigh() : 0)
                                                        + (reportSummary.getHigh() != null ? reportSummary.getHigh() : 0))
                                                .medium((aggSummary.getMedium() != null ? aggSummary.getMedium() : 0)
                                                        + (reportSummary.getMedium() != null ? reportSummary.getMedium() : 0))
                                                .low((aggSummary.getLow() != null ? aggSummary.getLow() : 0)
                                                        + (reportSummary.getLow() != null ? reportSummary.getLow() : 0))
                                                .remediations((aggSummary.getRemediations() != null ? aggSummary.getRemediations() : 0)
                                                        + (reportSummary.getRemediations() != null ? reportSummary.getRemediations() : 0))
                                                .recommendations((aggSummary.getRecommendations() != null ? aggSummary.getRecommendations() : 0)
                                                        + (reportSummary.getRecommendations() != null ? reportSummary.getRecommendations() : 0));
                                    }
                                } else {
                                    aggSource.setSummary(reportSummary);
                                }

                                List<DependencyReport> reportDependencies = sValue.getDependencies();
                                List<DependencyReport> aggDependencies = aggSource.getDependencies();
                                if (aggDependencies != null) {
                                    if (reportDependencies != null) {
                                        aggDependencies.addAll(reportDependencies);
                                    }
                                } else {
                                    aggSource.setDependencies(reportDependencies);
                                }
                            } else {
                                aggSources.put(sKey, sValue);
                            }
                        });
                    }
                } else {
                    if (value != null) {
                        aggProviders.put(key, value);
                    } else {
                        aggProviders.put(key, new ProviderReport().status(new ProviderStatus()).sources(new HashMap<>()));
                    }
                }
            });
        }

        return aggregated;
    }
}
