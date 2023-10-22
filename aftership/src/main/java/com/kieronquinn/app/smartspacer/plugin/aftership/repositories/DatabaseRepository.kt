package com.kieronquinn.app.smartspacer.plugin.aftership.repositories

import com.kieronquinn.app.smartspacer.plugin.aftership.model.database.AftershipDatabase
import com.kieronquinn.app.smartspacer.plugin.aftership.model.database.Package
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

abstract class DatabaseRepository(
    targetDataDao: TargetDataDao
): DatabaseRepositoryImpl(_targetData = targetDataDao) {

    abstract fun getPackages(): Flow<List<Package>>
    abstract suspend fun addPackage(pkg: Package)
    abstract suspend fun deletePackage(pkg: Package)

}

class DatabaseRepositoryImpl(
    database: AftershipDatabase
): DatabaseRepository(database.targetDataDao()) {

    private val packageDao = database.packageDao()

    override fun getPackages(): Flow<List<Package>> {
        return packageDao.getAll()
    }

    override suspend fun addPackage(pkg: Package) {
        withContext(Dispatchers.IO) {
            packageDao.insert(pkg)
        }
    }

    override suspend fun deletePackage(pkg: Package) {
        withContext(Dispatchers.IO) {
            pkg.icon.delete()
            pkg.image?.delete()
            pkg.map?.delete()
            packageDao.delete(pkg.id)
        }
    }

}