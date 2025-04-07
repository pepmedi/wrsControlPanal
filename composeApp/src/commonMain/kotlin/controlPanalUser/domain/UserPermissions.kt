package controlPanalUser.domain

import SidebarItem

object PanelUserPermissions {
    val defaultPermissions = mapOf(
        SidebarItem.DASHBOARD.title to false,
        SidebarItem.CPANEL_USERS.title to false,
        SidebarItem.APPOINTMENTS.title to false
    )
}