package com.example.intuitivebnb

object SessionManager {
    private var loggedInUser: String? = null
    private var userRole: String? = null

    /**
     * Guarda el nombre del usuario y su rol al iniciar sesión.
     * @param userName Nombre del usuario.
     * @param role Rol del usuario ("Guest" o "Host").
     */
    fun login(userName: String, role: String) {
        loggedInUser = userName
        userRole = role
    }

    /**
     * Cierra la sesión del usuario.
     */
    fun logout() {
        loggedInUser = null
        userRole = null
    }

    /**
     * Verifica si hay un usuario logueado.
     * @return true si hay un usuario logueado, false en caso contrario.
     */
    fun isUserLoggedIn(): Boolean {
        return loggedInUser != null
    }

    /**
     * Obtiene el nombre del usuario logueado.
     * @return Nombre del usuario o null si no hay sesión activa.
     */
    fun getLoggedInUser(): String? {
        return loggedInUser
    }

    /**
     * Obtiene el rol del usuario logueado.
     * @return Rol del usuario ("Guest" o "Host") o null si no hay sesión activa.
     */
    fun getUserRole(): String? {
        return userRole
    }

    /**
     * Verifica si el usuario es un "Host".
     * @return true si el usuario es un Host, false si es un Guest o no ha iniciado sesión.
     */
    fun isHost(): Boolean {
        return userRole == "Host"
    }

    /**
     * Verifica si el usuario es un "Guest".
     * @return true si el usuario es un Guest, false si es un Host o no ha iniciado sesión.
     */
    fun isGuest(): Boolean {
        return userRole == "Guest"
    }
}
