package com.ecommerce.shared.constants;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: WebConstants, NavConstants, ResultTableConstants
// BC References: BC-036 (session/token timeout), BC-046 (correlation ID header),
//                BC-051 (navigation sections), BC-052 (result table panel names / pagination)

/**
 * Application-wide constants used across all microservices.
 */
public final class AppConstants {

    private AppConstants() {}

    // ── HTTP Headers (BC-046) ────────────────────────────────────────────────
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String AUTHORIZATION_HEADER  = "Authorization";
    public static final String BEARER_PREFIX         = "Bearer ";

    // ── MDC key (BC-047) ─────────────────────────────────────────────────────
    public static final String MDC_CORRELATION_ID = "correlationId";

    // ── JWT Cookie names (BC-036) ────────────────────────────────────────────
    public static final String ACCESS_TOKEN_COOKIE  = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    // ── Default pagination (BC-052: old panels had max 3 or 5 rows) ──────────
    /** Customer summary panels: ORDER_HISTORY_PANEL, PAYMENT_METHODS_PANEL, WISHLIST_PANEL,
     *  BUSINESS_ACCOUNT_PANEL used maxRows=3 */
    public static final int DEFAULT_PAGE_SIZE_SMALL = 3;
    /** CUSTOMER_PROFILE_PANEL, CUSTOMER_PREFERENCES_PANEL, ORDERS_PANEL used maxRows=5 */
    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int MAX_PAGE_SIZE = 100;

    // ── Navigation sections (BC-051) ─────────────────────────────────────────
    public static final String NAV_CUSTOMER = "customer";
    public static final String NAV_CATALOG  = "catalog";
    public static final String NAV_ORDER    = "order";
    public static final String NAV_HOME     = "home";
    public static final String NAV_ADMIN    = "admin";

    // ── Result panel names (BC-052) ───────────────────────────────────────────
    public static final String PANEL_CUSTOMER_PROFILE      = "CUSTOMER_PROFILE_PANEL";
    public static final String PANEL_CUSTOMER_PREFERENCES  = "CUSTOMER_PREFERENCES_PANEL";
    public static final String PANEL_ORDER_HISTORY         = "ORDER_HISTORY_PANEL";
    public static final String PANEL_PAYMENT_METHODS       = "PAYMENT_METHODS_PANEL";
    public static final String PANEL_ORDERS                = "ORDERS_PANEL";
    public static final String PANEL_WISHLIST              = "WISHLIST_PANEL";
    public static final String PANEL_BUSINESS_ACCOUNT      = "BUSINESS_ACCOUNT_PANEL";

    // ── Post-save navigation hints (BC-014, BC-015, BC-056) ──────────────────
    /** Returned in response body to tell client to navigate to customer summary */
    public static final String REDIRECT_CUSTOMER_SUMMARY  = "CUSTOMER_SUMMARY";
    /** Returned in response body to tell client to navigate to maintain order history list */
    public static final String REDIRECT_ORDER_HISTORY_LIST = "ORDER_HISTORY_LIST";
    /** No navigation needed (webService mode or non-CUSTOMER type) */
    public static final String REDIRECT_NONE              = "NONE";
}
