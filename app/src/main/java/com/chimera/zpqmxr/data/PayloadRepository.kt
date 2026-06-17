package com.chimera.zpqmxr.data

import kotlinx.coroutines.flow.Flow

class PayloadRepository(private val payloadDao: PayloadDao) {
    val allPayloads: Flow<List<Payload>> = payloadDao.getAllPayloads()

    suspend fun insert(payload: Payload) = payloadDao.insertPayload(payload)

    suspend fun deleteById(id: Int) = payloadDao.deletePayloadById(id)
}
