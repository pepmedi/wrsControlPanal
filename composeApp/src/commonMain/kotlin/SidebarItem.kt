enum class SidebarItem(val title: String) {
    DASHBOARD("Dashboard"),
    UPDATE_INFO("Update Info"),
    SHOP("Shop"),
    PRODUCT("Product"),
    SHOP_VIDEO("Shop Video"),
    TICKETS("Tickets"),
    BANNER("Banner"),
    CPANEL_USERS("C-Panel Users"),
    APP_USERS("Application Users"),
    SHOP_APPROVAL("Shop Approval"),
    PRODUCT_APPROVAL("Product Approval"),
    SHOP_VIDEO_APPROVAL("Shop Video Approval"),
    LOGOUT("Log out");

    companion object {
        fun fromTitle(title: String): SidebarItem? {
            return entries.find { it.title == title }
        }
    }
}
