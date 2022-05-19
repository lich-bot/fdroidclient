package org.fdroid.index

import mu.KotlinLogging
import org.fdroid.CompatibilityChecker
import org.fdroid.database.FDroidDatabase
import org.fdroid.database.Repository
import org.fdroid.download.DownloaderFactory
import org.fdroid.index.v1.IndexV1Updater
import org.fdroid.index.v2.IndexV2Updater
import java.io.File
import java.io.FileNotFoundException

public class RepoUpdater(
    tempDir: File,
    db: FDroidDatabase,
    downloaderFactory: DownloaderFactory,
    compatibilityChecker: CompatibilityChecker,
    listener: IndexUpdateListener,
) {
    private val log = KotlinLogging.logger {}
    private val tempFileProvider = TempFileProvider {
        File.createTempFile("dl-", "", tempDir)
    }

    /**
     * A list of [IndexUpdater]s to try, sorted by newest first.
     */
    private val indexUpdater = listOf(
        IndexV2Updater(db, tempFileProvider, downloaderFactory, compatibilityChecker, listener),
        IndexV1Updater(db, tempFileProvider, downloaderFactory, compatibilityChecker, listener),
    )

    public fun update(
        repo: Repository,
        fingerprint: String? = null,
    ): IndexUpdateResult {
        return if (repo.certificate == null) {
            // This is a new repo without a certificate
            updateNewRepo(repo, fingerprint)
        } else {
            update(repo)
        }
    }

    private fun updateNewRepo(
        repo: Repository,
        expectedSigningFingerprint: String?,
    ): IndexUpdateResult = update(repo) { updater ->
        updater.updateNewRepo(repo, expectedSigningFingerprint)
    }

    private fun update(repo: Repository): IndexUpdateResult = update(repo) { updater ->
        updater.update(repo)
    }

    private fun update(
        repo: Repository,
        doUpdate: (IndexUpdater) -> IndexUpdateResult,
    ): IndexUpdateResult {
        indexUpdater.forEach { updater ->
            // don't downgrade to older updaters if repo used new format already
            val repoFormatVersion = repo.formatVersion
            if (repoFormatVersion != null && repoFormatVersion > updater.formatVersion) {
                val updaterVersion = updater.formatVersion.name
                log.warn { "Not using updater $updaterVersion for repo ${repo.address}" }
                return@forEach
            }
            val result = doUpdate(updater)
            if (result != IndexUpdateResult.NotFound) return result
        }
        return IndexUpdateResult.Error(FileNotFoundException("No files found for ${repo.address}"))
    }

}
