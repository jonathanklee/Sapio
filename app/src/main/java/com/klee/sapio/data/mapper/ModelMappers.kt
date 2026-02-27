package com.klee.sapio.data.mapper

import com.klee.sapio.data.dto.Evaluation
import com.klee.sapio.data.dto.IconAnswer
import com.klee.sapio.data.dto.StrapiElement
import com.klee.sapio.data.dto.UploadEvaluation
import com.klee.sapio.data.local.EvaluationEntity
import com.klee.sapio.data.local.IconEntity
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Evaluation as DomainEvaluation
import com.klee.sapio.domain.model.Icon as DomainIcon
import com.klee.sapio.domain.model.UploadEvaluation as DomainUploadEvaluation

fun Evaluation.toDomain(): DomainEvaluation = DomainEvaluation(
    name = name,
    packageName = packageName,
    iconUrl = icon?.data?.attributes?.url,
    rating = rating,
    microg = microg,
    secure = secure,
    updatedAt = updatedAt,
    createdAt = createdAt,
    publishedAt = publishedAt,
    versionName = versionName
)

fun Evaluation.toEntity(cachedAt: Long): EvaluationEntity = EvaluationEntity(
    name = name,
    packageName = packageName,
    iconUrl = icon?.data?.attributes?.url,
    rating = rating,
    microg = microg,
    secure = secure,
    updatedAt = updatedAt,
    createdAt = createdAt,
    publishedAt = publishedAt,
    versionName = versionName,
    cachedAt = cachedAt
)

fun EvaluationEntity.toDomain(): DomainEvaluation = DomainEvaluation(
    name = name,
    packageName = packageName,
    iconUrl = iconUrl,
    rating = rating,
    microg = microg,
    secure = secure,
    updatedAt = updatedAt,
    createdAt = createdAt,
    publishedAt = publishedAt,
    versionName = versionName
)

fun StrapiElement.toDomain(): EvaluationRecord = EvaluationRecord(
    id = id,
    evaluation = attributes.toDomain()
)

fun IconAnswer.toDomain(): DomainIcon = DomainIcon(
    id = id,
    name = name,
    url = url
)

fun IconAnswer.toEntity(cachedAt: Long): IconEntity = IconEntity(
    id = id,
    name = name,
    url = url,
    cachedAt = cachedAt
)

fun IconEntity.toDomain(): DomainIcon = DomainIcon(
    id = id,
    name = name,
    url = url
)

fun DomainUploadEvaluation.toData(): UploadEvaluation = UploadEvaluation(
    name = name,
    packageName = packageName,
    icon = icon,
    rating = rating,
    microg = microg,
    rooted = rooted
)
