package com.campuslf.models;

import java.util.Set;

public final class ReportStatus {
    public static final String LOST = "LOST";
    public static final String FOUND = "FOUND";
    public static final String CLAIMED = "CLAIMED";
    public static final String RESOLVED = "RESOLVED";
    public static final String UNCLAIMED = LOST;

    private static final Set<String> VALID_STATUSES = Set.of(LOST, FOUND, CLAIMED, RESOLVED);

    private ReportStatus() {
    }

    public static String normalize(String status) {
        if (status == null || status.isBlank()) {
            return LOST;
        }

        return switch (status.trim().toUpperCase()) {
            case "UNCLAIMED", "PENDING" -> LOST;
            case "CLAIMED", "APPROVED" -> CLAIMED;
            case "FOUND" -> FOUND;
            case "RESOLVED", "ARCHIVED" -> RESOLVED;
            case "LOST" -> LOST;
            default -> status.trim().toUpperCase();
        };
    }

    public static boolean isValid(String status) {
        return VALID_STATUSES.contains(normalize(status));
    }

    public static boolean isOpen(String status) {
        String normalized = normalize(status);
        return LOST.equals(normalized) || FOUND.equals(normalized);
    }

    public static boolean canBeClaimed(String status) {
        return FOUND.equals(normalize(status));
    }
}
