package controlPanalUser.domain

enum class UserRole {
    ADMIN,
    DOCTOR,
    EMPLOYEE
}

enum class RoleCode(val code: String) {
    ADMIN("0"),
    DOCTOR("1"),
    EMPLOYEE("2");

    companion object {
        fun fromCode(code: String): RoleCode = entries.firstOrNull { it.code == code }
            ?: throw IllegalArgumentException("Invalid role code: $code")
    }
}
