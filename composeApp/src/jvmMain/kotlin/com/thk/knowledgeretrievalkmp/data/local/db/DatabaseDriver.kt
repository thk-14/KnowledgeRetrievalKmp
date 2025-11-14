package com.thk.knowledgeretrievalkmp.data.local.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thk.knowledgeretrievalkmp.db.KbDatabase
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun getAppDataDirectory(appName: String): Path {
    val os = System.getProperty("os.name").lowercase()
    val home = System.getProperty("user.home")
    val dataDir: Path = when {
        os.contains("win") -> {
            Paths.get(System.getenv("APPDATA") ?: "$home\\AppData\\Roaming", appName)
        }

        os.contains("mac") -> {
            Paths.get(home, "Library", "Application Support", appName)
        }

        else -> {
            Paths.get(home, ".config", appName)
//            Paths.get(home, ".${appName.lowercase()}")
        }
    }
    // Create the directory if it doesn't exist
    if (Files.notExists(dataDir)) {
        try {
            Files.createDirectories(dataDir)
        } catch (e: Exception) {
            println("Error creating app data directory: $e")
            // Fallback to a temp directory
            return Paths.get(System.getProperty("java.io.tmpdir"), appName).also {
                Files.createDirectories(it)
            }
        }
    }
    return dataDir
}

fun getDatabasePath(appName: String, dbName: String): String {
    val dataDir = getAppDataDirectory(appName)
    return dataDir.resolve(dbName).toAbsolutePath().toString()
}

actual suspend fun createDriver(): SqlDriver {
//    val userHome = System.getProperty("user.home")
//    val dbFile = File(userHome, "KbDatabase.db")

    val dbPath = getDatabasePath("KMS", "KbDatabase.db")
    val dbFile = File(dbPath)
    val newDb = !dbFile.exists()

    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    if (newDb) {
        KbDatabase.Schema.create(driver).await()
    }
    return driver
}