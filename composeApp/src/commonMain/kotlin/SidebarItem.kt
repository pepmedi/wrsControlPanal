enum class SidebarItem(val title: String) {
    DASHBOARD("Dashboard"),
//    UPDATE_INFO("Update Info"),
    APPOINTMENTS("Appointments"),
    BLOGS("Blogs"),
    SERVICES("Services"),
    HOSPITALS("Hospitals"),
    ACHIEVEMENTS("Achievements"),
    DOCTORS("Doctors"),
    CPANEL_USERS("C-Panel Users"),
    LOGOUT("Log out");

    companion object {
        fun fromTitle(title: String): SidebarItem? {
            return entries.find { it.title == title }
        }
    }
}
