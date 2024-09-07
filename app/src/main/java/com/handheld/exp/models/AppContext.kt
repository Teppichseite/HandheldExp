package com.handheld.exp.models

data class AppContext(
    val taskId: Int?,
    val name: String,
    val packageName: String
){

    companion object{
        public const val ES_DE_PACKAGE_NAME = "org.es_de.frontend"
        public const val OWN_PACKAGE_NAME = "com.handheld.exp"
    }

    val isEsDe : Boolean
        get() = packageName == ES_DE_PACKAGE_NAME

    val isSelf : Boolean
        get() = packageName == OWN_PACKAGE_NAME
}
