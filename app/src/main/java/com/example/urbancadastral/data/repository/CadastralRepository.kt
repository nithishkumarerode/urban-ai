package com.example.urbancadastral.data.repository

import com.example.urbancadastral.data.local.dao.CadastralDao
import com.example.urbancadastral.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class CadastralRepository(private val dao: CadastralDao) {

    // Projects
    val allProjects: Flow<List<CadastralProjectEntity>> = dao.getAllProjects()
    suspend fun insertProject(project: CadastralProjectEntity) = dao.insertProject(project)

    // Drone Datasets
    val allDatasets: Flow<List<DroneDatasetEntity>> = dao.getAllDatasets()
    suspend fun insertDataset(dataset: DroneDatasetEntity) = dao.insertDataset(dataset)
    suspend fun deleteDataset(dataset: DroneDatasetEntity) = dao.deleteDataset(dataset)

    // Processing Jobs
    val allJobs: Flow<List<ProcessingJobEntity>> = dao.getAllProcessingJobs()
    suspend fun insertJob(job: ProcessingJobEntity) = dao.insertJob(job)
    suspend fun updateJob(job: ProcessingJobEntity) = dao.updateJob(job)

    // Parcels
    val allParcels: Flow<List<ParcelEntity>> = dao.getAllParcels()
    fun getParcelsByStatus(status: String) = dao.getParcelsByStatus(status)
    fun getParcelsByConfidenceTier(tier: String) = dao.getParcelsByConfidenceTier(tier)
    suspend fun insertParcel(parcel: ParcelEntity) = dao.insertParcel(parcel)
    suspend fun updateParcel(parcel: ParcelEntity) = dao.updateParcel(parcel)

    // Building Footprints
    val allFootprints: Flow<List<BuildingFootprintEntity>> = dao.getAllBuildingFootprints()

    // Conflicts
    val allConflicts: Flow<List<BoundaryConflictEntity>> = dao.getAllConflicts()
    suspend fun insertConflict(conflict: BoundaryConflictEntity) = dao.insertConflict(conflict)
    suspend fun updateConflict(conflict: BoundaryConflictEntity) = dao.updateConflict(conflict)

    // Ground Truth Tasks
    val allTasks: Flow<List<GroundTruthTaskEntity>> = dao.getAllGroundTruthTasks()
    suspend fun insertTask(task: GroundTruthTaskEntity) = dao.insertGroundTruthTask(task)
    suspend fun updateTask(task: GroundTruthTaskEntity) = dao.updateGroundTruthTask(task)

    // Evidence
    fun getEvidenceForTask(taskId: Long): Flow<List<SurveyEvidenceEntity>> = dao.getEvidenceForTask(taskId)
    val allEvidence: Flow<List<SurveyEvidenceEntity>> = dao.getAllEvidence()
    suspend fun insertEvidence(evidence: SurveyEvidenceEntity) = dao.insertEvidence(evidence)
    suspend fun deleteEvidence(evidence: SurveyEvidenceEntity) = dao.deleteEvidence(evidence)
    suspend fun deleteEvidenceById(id: Long) = dao.deleteEvidenceById(id)
}
