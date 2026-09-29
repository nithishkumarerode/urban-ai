package com.example.urbancadastral.data.local.dao

import androidx.room.*
import com.example.urbancadastral.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CadastralDao {

    // Projects
    @Query("SELECT * FROM cadastral_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<CadastralProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: CadastralProjectEntity): Long

    @Query("SELECT * FROM cadastral_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): CadastralProjectEntity?

    // Drone Datasets
    @Query("SELECT * FROM drone_datasets ORDER BY uploadTimestamp DESC")
    fun getAllDatasets(): Flow<List<DroneDatasetEntity>>

    @Query("SELECT * FROM drone_datasets WHERE projectId = :projectId ORDER BY uploadTimestamp DESC")
    fun getDatasetsByProject(projectId: Long): Flow<List<DroneDatasetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: DroneDatasetEntity): Long

    @Delete
    suspend fun deleteDataset(dataset: DroneDatasetEntity)

    // Processing Jobs
    @Query("SELECT * FROM processing_jobs ORDER BY startTime DESC")
    fun getAllProcessingJobs(): Flow<List<ProcessingJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: ProcessingJobEntity): Long

    @Update
    suspend fun updateJob(job: ProcessingJobEntity)

    @Query("SELECT * FROM processing_jobs WHERE id = :id")
    suspend fun getJobById(id: Long): ProcessingJobEntity?

    // Parcels
    @Query("SELECT * FROM parcels ORDER BY id ASC")
    fun getAllParcels(): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE projectId = :projectId ORDER BY id ASC")
    fun getParcelsByProject(projectId: Long): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE verificationStatus = :status")
    fun getParcelsByStatus(status: String): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE confidenceTier = :tier")
    fun getParcelsByConfidenceTier(tier: String): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE id = :id")
    suspend fun getParcelById(id: Long): ParcelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcels(parcels: List<ParcelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcel(parcel: ParcelEntity): Long

    @Update
    suspend fun updateParcel(parcel: ParcelEntity)

    // Building Footprints
    @Query("SELECT * FROM building_footprints")
    fun getAllBuildingFootprints(): Flow<List<BuildingFootprintEntity>>

    @Query("SELECT * FROM building_footprints WHERE parcelId = :parcelId")
    fun getFootprintsByParcel(parcelId: Long): Flow<List<BuildingFootprintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFootprints(footprints: List<BuildingFootprintEntity>)

    // Boundary Conflicts
    @Query("SELECT * FROM boundary_conflicts ORDER BY severity DESC, detectedDate DESC")
    fun getAllConflicts(): Flow<List<BoundaryConflictEntity>>

    @Query("SELECT * FROM boundary_conflicts WHERE status = :status")
    fun getConflictsByStatus(status: String): Flow<List<BoundaryConflictEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: BoundaryConflictEntity): Long

    @Update
    suspend fun updateConflict(conflict: BoundaryConflictEntity)

    // Ground Truth Tasks
    @Query("SELECT * FROM ground_truth_tasks ORDER BY status ASC, id DESC")
    fun getAllGroundTruthTasks(): Flow<List<GroundTruthTaskEntity>>

    @Query("SELECT * FROM ground_truth_tasks WHERE id = :id")
    suspend fun getGroundTruthTaskById(id: Long): GroundTruthTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroundTruthTask(task: GroundTruthTaskEntity): Long

    @Update
    suspend fun updateGroundTruthTask(task: GroundTruthTaskEntity)

    // Survey Evidence
    @Query("SELECT * FROM survey_evidence WHERE taskId = :taskId ORDER BY uploadedAt DESC")
    fun getEvidenceForTask(taskId: Long): Flow<List<SurveyEvidenceEntity>>

    @Query("SELECT * FROM survey_evidence ORDER BY uploadedAt DESC")
    fun getAllEvidence(): Flow<List<SurveyEvidenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: SurveyEvidenceEntity): Long

    @Delete
    suspend fun deleteEvidence(evidence: SurveyEvidenceEntity)

    @Query("DELETE FROM survey_evidence WHERE id = :id")
    suspend fun deleteEvidenceById(id: Long)
}
