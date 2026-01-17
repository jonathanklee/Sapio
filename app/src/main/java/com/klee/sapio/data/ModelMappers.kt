package com.klee.sapio.data

import com.klee.sapio.domain.model.Evaluation as DomainEvaluation
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Icon as DomainIcon
import com.klee.sapio.domain.model.UploadEvaluation as DomainUploadEvaluation

fun Evaluation.toDomain(): DomainEvaluation = DomainEvaluation(
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

fun DomainUploadEvaluation.toData(): UploadEvaluation = UploadEvaluation(
    name = name,
    packageName = packageName,
    icon = icon,
    rating = rating,
    microg = microg,
    rooted = rooted
)
