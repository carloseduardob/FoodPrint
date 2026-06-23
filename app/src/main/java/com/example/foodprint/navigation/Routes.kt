package com.example.foodprint.navigation

sealed class Routes(val route: String) {
    object Dashboard : Routes("dashboard")
    object Inventory : Routes("inventory")
    object Scanner : Routes("scanner")
    object ScannerReview : Routes("scanner_review")
    object Chef : Routes("chef")
    object Shopping : Routes("shopping")
    object ScannerWebView : Routes("scanner_webview/{url}") {
        fun createRoute(url: String) = "scanner_webview/${java.net.URLEncoder.encode(url, "UTF-8")}"
    }
}