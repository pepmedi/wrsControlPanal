package controlPanalUser.repository

import controlPanalUser.domain.UserSession

object SessionManager {
    var currentUser: UserSession? = null
}