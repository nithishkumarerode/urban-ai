package com.example.urbancadastral.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cadastral_projects")
data class CadastralProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val jurisdiction: String,
    val crs: String,
    val targetParcels: Int,
    val status: String, // ACTIVE, PROCESSING, AUDIT, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "drone_datasets")
data class DroneDatasetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val fileType: String, // GeoTIFF, DSM/DTM, LAS PointCloud, High-Res Ortho
    val fileSizeMb: Double,
    val crs: String,
    val gsdCm: Double,
    val resolution: String,
    val coverageHa: Double,
    val extentBBox: String,
    val sensorModel: String,
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val validationStatus: String, // VALIDATED, PENDING_CHECK, INVALID
    val isProductionReady: Boolean = true
)

@Entity(tableName = "processing_jobs")
data class ProcessingJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val datasetId: Long,
    val datasetName: String,
    val stage: String,
    val progressPercent: Int,
    val status: String, // QUEUED, RUNNING, COMPLETED, FAILED
    val isDevSimulation: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),
    val updatedTime: Long = System.currentTimeMillis(),
    val extractedParcelsCount: Int = 0,
    val extractedBuildingsCount: Int = 0,
    val conflictsDetectedCount: Int = 0
)

@Entity(tableName = "parcels")
data class ParcelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val parcelNumber: String,
    val legalDeedOwner: String,
    val landUse: String, // Residential, Commercial, Industrial, Public Utility, Agricultural
    val areaSqm: Double,
    val perimeterMeters: Double,
    val confidenceScore: Float,
    val confidenceTier: String, // HIGH, MEDIUM, LOW
    val verificationStatus: String, // AUTO_VERIFIED, HUMAN_APPROVED, NEEDS_REVIEW, CONFLICT_FLAGGED, GROUND_TRUTH_SEALED
    val geometryJson: String, // Normalized 2D coordinates for canvas rendering
    val hasConflict: Boolean = false,
    val conflictId: Long? = null,
    val zoningCode: String = "R-2 Urban Zone",
    val centroidLat: Double = 52.5200,
    val centroidLng: Double = 13.4050
)

@Entity(tableName = "building_footprints")
data class BuildingFootprintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parcelId: Long,
    val structureType: String,
    val footprintAreaSqm: Double,
    val estimatedHeightMeters: Double,
    val confidenceScore: Float,
    val isEncroaching: Boolean = false
)

@Entity(tableName = "boundary_conflicts")
data class BoundaryConflictEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parcelId: Long,
    val parcelNumber: String,
    val adjacentParcelNumber: String,
    val conflictType: String, // Deed Mismatch, Physical Encroachment, Right-of-Way Intrusion
    val disputedAreaSqm: Double,
    val encroachmentDistanceM: Double,
    val severity: String, // CRITICAL, MODERATE, LOW
    val status: String, // OPEN, IN_FIELD_SURVEY, RESOLVED, REJECTED
    val detectedDate: Long = System.currentTimeMillis(),
    val notes: String
)

@Entity(tableName = "ground_truth_tasks")
data class GroundTruthTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parcelId: Long,
    val parcelNumber: String,
    val discrepancyReason: String,
    val assignedSurveyor: String,
    val gnssAccuracyCm: Double,
    val corsStationId: String,
    val status: String, // PENDING, FIELD_IN_PROGRESS, VERIFIED_SEALED
    val sealedTimestamp: Long? = null,
    val sealedDigitalSignature: String? = null
)

@Entity(tableName = "survey_evidence")
data class SurveyEvidenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val fileName: String,
    val fileType: String, // GNSS_RTK, FIELD_PHOTO, DEED_PDF, TOTAL_STATION_CSV
    val fileSizeKb: Long,
    val uploadedAt: Long = System.currentTimeMillis(),
    val uploadedBy: String,
    val notes: String = "",
    val fileUri: String = ""
)
