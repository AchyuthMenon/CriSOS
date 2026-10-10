package com.crisos.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SosMessageDao_Impl implements SosMessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SosMessageEntity> __insertionAdapterOfSosMessageEntity;

  private final EntityDeletionOrUpdateAdapter<SosMessageEntity> __deletionAdapterOfSosMessageEntity;

  private final EntityDeletionOrUpdateAdapter<SosMessageEntity> __updateAdapterOfSosMessageEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateCloudSyncStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDeliveryStatus;

  public SosMessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSosMessageEntity = new EntityInsertionAdapter<SosMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sos_messages` (`id`,`messageType`,`senderDeviceId`,`emergencyCategory`,`description`,`assistanceTypes`,`latitude`,`longitude`,`locationAccuracyMeters`,`locationTimestamp`,`createdAt`,`expiresAt`,`hopCount`,`maxHopCount`,`deliveryStatus`,`direction`,`cloudSyncStatus`,`cloudSyncedAt`,`incidentId`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SosMessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getMessageType());
        statement.bindString(3, entity.getSenderDeviceId());
        statement.bindString(4, entity.getEmergencyCategory());
        statement.bindString(5, entity.getDescription());
        statement.bindString(6, entity.getAssistanceTypes());
        if (entity.getLatitude() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getLongitude());
        }
        if (entity.getLocationAccuracyMeters() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getLocationAccuracyMeters());
        }
        if (entity.getLocationTimestamp() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getLocationTimestamp());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getExpiresAt());
        statement.bindLong(13, entity.getHopCount());
        statement.bindLong(14, entity.getMaxHopCount());
        statement.bindString(15, entity.getDeliveryStatus());
        statement.bindString(16, entity.getDirection());
        statement.bindString(17, entity.getCloudSyncStatus());
        if (entity.getCloudSyncedAt() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getCloudSyncedAt());
        }
        if (entity.getIncidentId() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getIncidentId());
        }
      }
    };
    this.__deletionAdapterOfSosMessageEntity = new EntityDeletionOrUpdateAdapter<SosMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `sos_messages` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SosMessageEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfSosMessageEntity = new EntityDeletionOrUpdateAdapter<SosMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sos_messages` SET `id` = ?,`messageType` = ?,`senderDeviceId` = ?,`emergencyCategory` = ?,`description` = ?,`assistanceTypes` = ?,`latitude` = ?,`longitude` = ?,`locationAccuracyMeters` = ?,`locationTimestamp` = ?,`createdAt` = ?,`expiresAt` = ?,`hopCount` = ?,`maxHopCount` = ?,`deliveryStatus` = ?,`direction` = ?,`cloudSyncStatus` = ?,`cloudSyncedAt` = ?,`incidentId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SosMessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getMessageType());
        statement.bindString(3, entity.getSenderDeviceId());
        statement.bindString(4, entity.getEmergencyCategory());
        statement.bindString(5, entity.getDescription());
        statement.bindString(6, entity.getAssistanceTypes());
        if (entity.getLatitude() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getLongitude());
        }
        if (entity.getLocationAccuracyMeters() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getLocationAccuracyMeters());
        }
        if (entity.getLocationTimestamp() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getLocationTimestamp());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getExpiresAt());
        statement.bindLong(13, entity.getHopCount());
        statement.bindLong(14, entity.getMaxHopCount());
        statement.bindString(15, entity.getDeliveryStatus());
        statement.bindString(16, entity.getDirection());
        statement.bindString(17, entity.getCloudSyncStatus());
        if (entity.getCloudSyncedAt() == null) {
          statement.bindNull(18);
        } else {
          statement.bindLong(18, entity.getCloudSyncedAt());
        }
        if (entity.getIncidentId() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getIncidentId());
        }
        statement.bindString(20, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateCloudSyncStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sos_messages SET cloudSyncStatus = ?, cloudSyncedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateDeliveryStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sos_messages SET deliveryStatus = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final SosMessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSosMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final SosMessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSosMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final SosMessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSosMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCloudSyncStatus(final String messageId, final String status,
      final Long syncedAt, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateCloudSyncStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        if (syncedAt == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, syncedAt);
        }
        _argIndex = 3;
        _stmt.bindString(_argIndex, messageId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateCloudSyncStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDeliveryStatus(final String messageId, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDeliveryStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindString(_argIndex, messageId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateDeliveryStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SosMessageEntity>> getAllMessages() {
    final String _sql = "SELECT * FROM sos_messages ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sos_messages"}, new Callable<List<SosMessageEntity>>() {
      @Override
      @NonNull
      public List<SosMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSenderDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDeviceId");
          final int _cursorIndexOfEmergencyCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyCategory");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAssistanceTypes = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceTypes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracyMeters");
          final int _cursorIndexOfLocationTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "locationTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfMaxHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHopCount");
          final int _cursorIndexOfDeliveryStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryStatus");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfCloudSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncStatus");
          final int _cursorIndexOfCloudSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncedAt");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final List<SosMessageEntity> _result = new ArrayList<SosMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SosMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpSenderDeviceId;
            _tmpSenderDeviceId = _cursor.getString(_cursorIndexOfSenderDeviceId);
            final String _tmpEmergencyCategory;
            _tmpEmergencyCategory = _cursor.getString(_cursorIndexOfEmergencyCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAssistanceTypes;
            _tmpAssistanceTypes = _cursor.getString(_cursorIndexOfAssistanceTypes);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpLocationAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfLocationAccuracyMeters)) {
              _tmpLocationAccuracyMeters = null;
            } else {
              _tmpLocationAccuracyMeters = _cursor.getFloat(_cursorIndexOfLocationAccuracyMeters);
            }
            final Long _tmpLocationTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocationTimestamp)) {
              _tmpLocationTimestamp = null;
            } else {
              _tmpLocationTimestamp = _cursor.getLong(_cursorIndexOfLocationTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpMaxHopCount;
            _tmpMaxHopCount = _cursor.getInt(_cursorIndexOfMaxHopCount);
            final String _tmpDeliveryStatus;
            _tmpDeliveryStatus = _cursor.getString(_cursorIndexOfDeliveryStatus);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpCloudSyncStatus;
            _tmpCloudSyncStatus = _cursor.getString(_cursorIndexOfCloudSyncStatus);
            final Long _tmpCloudSyncedAt;
            if (_cursor.isNull(_cursorIndexOfCloudSyncedAt)) {
              _tmpCloudSyncedAt = null;
            } else {
              _tmpCloudSyncedAt = _cursor.getLong(_cursorIndexOfCloudSyncedAt);
            }
            final String _tmpIncidentId;
            if (_cursor.isNull(_cursorIndexOfIncidentId)) {
              _tmpIncidentId = null;
            } else {
              _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            }
            _item = new SosMessageEntity(_tmpId,_tmpMessageType,_tmpSenderDeviceId,_tmpEmergencyCategory,_tmpDescription,_tmpAssistanceTypes,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracyMeters,_tmpLocationTimestamp,_tmpCreatedAt,_tmpExpiresAt,_tmpHopCount,_tmpMaxHopCount,_tmpDeliveryStatus,_tmpDirection,_tmpCloudSyncStatus,_tmpCloudSyncedAt,_tmpIncidentId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SosMessageEntity>> getSentMessages() {
    final String _sql = "SELECT * FROM sos_messages WHERE direction = 'OUTGOING' ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sos_messages"}, new Callable<List<SosMessageEntity>>() {
      @Override
      @NonNull
      public List<SosMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSenderDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDeviceId");
          final int _cursorIndexOfEmergencyCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyCategory");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAssistanceTypes = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceTypes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracyMeters");
          final int _cursorIndexOfLocationTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "locationTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfMaxHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHopCount");
          final int _cursorIndexOfDeliveryStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryStatus");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfCloudSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncStatus");
          final int _cursorIndexOfCloudSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncedAt");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final List<SosMessageEntity> _result = new ArrayList<SosMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SosMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpSenderDeviceId;
            _tmpSenderDeviceId = _cursor.getString(_cursorIndexOfSenderDeviceId);
            final String _tmpEmergencyCategory;
            _tmpEmergencyCategory = _cursor.getString(_cursorIndexOfEmergencyCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAssistanceTypes;
            _tmpAssistanceTypes = _cursor.getString(_cursorIndexOfAssistanceTypes);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpLocationAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfLocationAccuracyMeters)) {
              _tmpLocationAccuracyMeters = null;
            } else {
              _tmpLocationAccuracyMeters = _cursor.getFloat(_cursorIndexOfLocationAccuracyMeters);
            }
            final Long _tmpLocationTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocationTimestamp)) {
              _tmpLocationTimestamp = null;
            } else {
              _tmpLocationTimestamp = _cursor.getLong(_cursorIndexOfLocationTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpMaxHopCount;
            _tmpMaxHopCount = _cursor.getInt(_cursorIndexOfMaxHopCount);
            final String _tmpDeliveryStatus;
            _tmpDeliveryStatus = _cursor.getString(_cursorIndexOfDeliveryStatus);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpCloudSyncStatus;
            _tmpCloudSyncStatus = _cursor.getString(_cursorIndexOfCloudSyncStatus);
            final Long _tmpCloudSyncedAt;
            if (_cursor.isNull(_cursorIndexOfCloudSyncedAt)) {
              _tmpCloudSyncedAt = null;
            } else {
              _tmpCloudSyncedAt = _cursor.getLong(_cursorIndexOfCloudSyncedAt);
            }
            final String _tmpIncidentId;
            if (_cursor.isNull(_cursorIndexOfIncidentId)) {
              _tmpIncidentId = null;
            } else {
              _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            }
            _item = new SosMessageEntity(_tmpId,_tmpMessageType,_tmpSenderDeviceId,_tmpEmergencyCategory,_tmpDescription,_tmpAssistanceTypes,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracyMeters,_tmpLocationTimestamp,_tmpCreatedAt,_tmpExpiresAt,_tmpHopCount,_tmpMaxHopCount,_tmpDeliveryStatus,_tmpDirection,_tmpCloudSyncStatus,_tmpCloudSyncedAt,_tmpIncidentId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SosMessageEntity>> getReceivedMessages() {
    final String _sql = "SELECT * FROM sos_messages WHERE direction = 'INCOMING' ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sos_messages"}, new Callable<List<SosMessageEntity>>() {
      @Override
      @NonNull
      public List<SosMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSenderDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDeviceId");
          final int _cursorIndexOfEmergencyCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyCategory");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAssistanceTypes = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceTypes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracyMeters");
          final int _cursorIndexOfLocationTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "locationTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfMaxHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHopCount");
          final int _cursorIndexOfDeliveryStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryStatus");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfCloudSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncStatus");
          final int _cursorIndexOfCloudSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncedAt");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final List<SosMessageEntity> _result = new ArrayList<SosMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SosMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpSenderDeviceId;
            _tmpSenderDeviceId = _cursor.getString(_cursorIndexOfSenderDeviceId);
            final String _tmpEmergencyCategory;
            _tmpEmergencyCategory = _cursor.getString(_cursorIndexOfEmergencyCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAssistanceTypes;
            _tmpAssistanceTypes = _cursor.getString(_cursorIndexOfAssistanceTypes);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpLocationAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfLocationAccuracyMeters)) {
              _tmpLocationAccuracyMeters = null;
            } else {
              _tmpLocationAccuracyMeters = _cursor.getFloat(_cursorIndexOfLocationAccuracyMeters);
            }
            final Long _tmpLocationTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocationTimestamp)) {
              _tmpLocationTimestamp = null;
            } else {
              _tmpLocationTimestamp = _cursor.getLong(_cursorIndexOfLocationTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpMaxHopCount;
            _tmpMaxHopCount = _cursor.getInt(_cursorIndexOfMaxHopCount);
            final String _tmpDeliveryStatus;
            _tmpDeliveryStatus = _cursor.getString(_cursorIndexOfDeliveryStatus);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpCloudSyncStatus;
            _tmpCloudSyncStatus = _cursor.getString(_cursorIndexOfCloudSyncStatus);
            final Long _tmpCloudSyncedAt;
            if (_cursor.isNull(_cursorIndexOfCloudSyncedAt)) {
              _tmpCloudSyncedAt = null;
            } else {
              _tmpCloudSyncedAt = _cursor.getLong(_cursorIndexOfCloudSyncedAt);
            }
            final String _tmpIncidentId;
            if (_cursor.isNull(_cursorIndexOfIncidentId)) {
              _tmpIncidentId = null;
            } else {
              _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            }
            _item = new SosMessageEntity(_tmpId,_tmpMessageType,_tmpSenderDeviceId,_tmpEmergencyCategory,_tmpDescription,_tmpAssistanceTypes,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracyMeters,_tmpLocationTimestamp,_tmpCreatedAt,_tmpExpiresAt,_tmpHopCount,_tmpMaxHopCount,_tmpDeliveryStatus,_tmpDirection,_tmpCloudSyncStatus,_tmpCloudSyncedAt,_tmpIncidentId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<SosMessageEntity> getMessageById(final String messageId) {
    final String _sql = "SELECT * FROM sos_messages WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, messageId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sos_messages"}, new Callable<SosMessageEntity>() {
      @Override
      @Nullable
      public SosMessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSenderDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDeviceId");
          final int _cursorIndexOfEmergencyCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyCategory");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAssistanceTypes = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceTypes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracyMeters");
          final int _cursorIndexOfLocationTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "locationTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfMaxHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHopCount");
          final int _cursorIndexOfDeliveryStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryStatus");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfCloudSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncStatus");
          final int _cursorIndexOfCloudSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncedAt");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final SosMessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpSenderDeviceId;
            _tmpSenderDeviceId = _cursor.getString(_cursorIndexOfSenderDeviceId);
            final String _tmpEmergencyCategory;
            _tmpEmergencyCategory = _cursor.getString(_cursorIndexOfEmergencyCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAssistanceTypes;
            _tmpAssistanceTypes = _cursor.getString(_cursorIndexOfAssistanceTypes);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpLocationAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfLocationAccuracyMeters)) {
              _tmpLocationAccuracyMeters = null;
            } else {
              _tmpLocationAccuracyMeters = _cursor.getFloat(_cursorIndexOfLocationAccuracyMeters);
            }
            final Long _tmpLocationTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocationTimestamp)) {
              _tmpLocationTimestamp = null;
            } else {
              _tmpLocationTimestamp = _cursor.getLong(_cursorIndexOfLocationTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpMaxHopCount;
            _tmpMaxHopCount = _cursor.getInt(_cursorIndexOfMaxHopCount);
            final String _tmpDeliveryStatus;
            _tmpDeliveryStatus = _cursor.getString(_cursorIndexOfDeliveryStatus);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpCloudSyncStatus;
            _tmpCloudSyncStatus = _cursor.getString(_cursorIndexOfCloudSyncStatus);
            final Long _tmpCloudSyncedAt;
            if (_cursor.isNull(_cursorIndexOfCloudSyncedAt)) {
              _tmpCloudSyncedAt = null;
            } else {
              _tmpCloudSyncedAt = _cursor.getLong(_cursorIndexOfCloudSyncedAt);
            }
            final String _tmpIncidentId;
            if (_cursor.isNull(_cursorIndexOfIncidentId)) {
              _tmpIncidentId = null;
            } else {
              _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            }
            _result = new SosMessageEntity(_tmpId,_tmpMessageType,_tmpSenderDeviceId,_tmpEmergencyCategory,_tmpDescription,_tmpAssistanceTypes,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracyMeters,_tmpLocationTimestamp,_tmpCreatedAt,_tmpExpiresAt,_tmpHopCount,_tmpMaxHopCount,_tmpDeliveryStatus,_tmpDirection,_tmpCloudSyncStatus,_tmpCloudSyncedAt,_tmpIncidentId);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMessageByIdOnce(final String messageId,
      final Continuation<? super SosMessageEntity> $completion) {
    final String _sql = "SELECT * FROM sos_messages WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, messageId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SosMessageEntity>() {
      @Override
      @Nullable
      public SosMessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSenderDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDeviceId");
          final int _cursorIndexOfEmergencyCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyCategory");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAssistanceTypes = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceTypes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracyMeters");
          final int _cursorIndexOfLocationTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "locationTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfMaxHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHopCount");
          final int _cursorIndexOfDeliveryStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryStatus");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfCloudSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncStatus");
          final int _cursorIndexOfCloudSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncedAt");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final SosMessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpSenderDeviceId;
            _tmpSenderDeviceId = _cursor.getString(_cursorIndexOfSenderDeviceId);
            final String _tmpEmergencyCategory;
            _tmpEmergencyCategory = _cursor.getString(_cursorIndexOfEmergencyCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAssistanceTypes;
            _tmpAssistanceTypes = _cursor.getString(_cursorIndexOfAssistanceTypes);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpLocationAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfLocationAccuracyMeters)) {
              _tmpLocationAccuracyMeters = null;
            } else {
              _tmpLocationAccuracyMeters = _cursor.getFloat(_cursorIndexOfLocationAccuracyMeters);
            }
            final Long _tmpLocationTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocationTimestamp)) {
              _tmpLocationTimestamp = null;
            } else {
              _tmpLocationTimestamp = _cursor.getLong(_cursorIndexOfLocationTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpMaxHopCount;
            _tmpMaxHopCount = _cursor.getInt(_cursorIndexOfMaxHopCount);
            final String _tmpDeliveryStatus;
            _tmpDeliveryStatus = _cursor.getString(_cursorIndexOfDeliveryStatus);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpCloudSyncStatus;
            _tmpCloudSyncStatus = _cursor.getString(_cursorIndexOfCloudSyncStatus);
            final Long _tmpCloudSyncedAt;
            if (_cursor.isNull(_cursorIndexOfCloudSyncedAt)) {
              _tmpCloudSyncedAt = null;
            } else {
              _tmpCloudSyncedAt = _cursor.getLong(_cursorIndexOfCloudSyncedAt);
            }
            final String _tmpIncidentId;
            if (_cursor.isNull(_cursorIndexOfIncidentId)) {
              _tmpIncidentId = null;
            } else {
              _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            }
            _result = new SosMessageEntity(_tmpId,_tmpMessageType,_tmpSenderDeviceId,_tmpEmergencyCategory,_tmpDescription,_tmpAssistanceTypes,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracyMeters,_tmpLocationTimestamp,_tmpCreatedAt,_tmpExpiresAt,_tmpHopCount,_tmpMaxHopCount,_tmpDeliveryStatus,_tmpDirection,_tmpCloudSyncStatus,_tmpCloudSyncedAt,_tmpIncidentId);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SosMessageEntity>> getPendingMessages() {
    final String _sql = "SELECT * FROM sos_messages WHERE deliveryStatus = 'QUEUED' OR deliveryStatus = 'FAILED' ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sos_messages"}, new Callable<List<SosMessageEntity>>() {
      @Override
      @NonNull
      public List<SosMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSenderDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDeviceId");
          final int _cursorIndexOfEmergencyCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyCategory");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAssistanceTypes = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceTypes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracyMeters");
          final int _cursorIndexOfLocationTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "locationTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfMaxHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHopCount");
          final int _cursorIndexOfDeliveryStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryStatus");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfCloudSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncStatus");
          final int _cursorIndexOfCloudSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncedAt");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final List<SosMessageEntity> _result = new ArrayList<SosMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SosMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpSenderDeviceId;
            _tmpSenderDeviceId = _cursor.getString(_cursorIndexOfSenderDeviceId);
            final String _tmpEmergencyCategory;
            _tmpEmergencyCategory = _cursor.getString(_cursorIndexOfEmergencyCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAssistanceTypes;
            _tmpAssistanceTypes = _cursor.getString(_cursorIndexOfAssistanceTypes);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpLocationAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfLocationAccuracyMeters)) {
              _tmpLocationAccuracyMeters = null;
            } else {
              _tmpLocationAccuracyMeters = _cursor.getFloat(_cursorIndexOfLocationAccuracyMeters);
            }
            final Long _tmpLocationTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocationTimestamp)) {
              _tmpLocationTimestamp = null;
            } else {
              _tmpLocationTimestamp = _cursor.getLong(_cursorIndexOfLocationTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpMaxHopCount;
            _tmpMaxHopCount = _cursor.getInt(_cursorIndexOfMaxHopCount);
            final String _tmpDeliveryStatus;
            _tmpDeliveryStatus = _cursor.getString(_cursorIndexOfDeliveryStatus);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpCloudSyncStatus;
            _tmpCloudSyncStatus = _cursor.getString(_cursorIndexOfCloudSyncStatus);
            final Long _tmpCloudSyncedAt;
            if (_cursor.isNull(_cursorIndexOfCloudSyncedAt)) {
              _tmpCloudSyncedAt = null;
            } else {
              _tmpCloudSyncedAt = _cursor.getLong(_cursorIndexOfCloudSyncedAt);
            }
            final String _tmpIncidentId;
            if (_cursor.isNull(_cursorIndexOfIncidentId)) {
              _tmpIncidentId = null;
            } else {
              _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            }
            _item = new SosMessageEntity(_tmpId,_tmpMessageType,_tmpSenderDeviceId,_tmpEmergencyCategory,_tmpDescription,_tmpAssistanceTypes,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracyMeters,_tmpLocationTimestamp,_tmpCreatedAt,_tmpExpiresAt,_tmpHopCount,_tmpMaxHopCount,_tmpDeliveryStatus,_tmpDirection,_tmpCloudSyncStatus,_tmpCloudSyncedAt,_tmpIncidentId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPendingOutgoingMessagesOnce(
      final Continuation<? super List<SosMessageEntity>> $completion) {
    final String _sql = "SELECT * FROM sos_messages WHERE (deliveryStatus = 'QUEUED' OR deliveryStatus = 'FAILED') AND direction = 'OUTGOING' ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SosMessageEntity>>() {
      @Override
      @NonNull
      public List<SosMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSenderDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDeviceId");
          final int _cursorIndexOfEmergencyCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyCategory");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAssistanceTypes = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceTypes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracyMeters");
          final int _cursorIndexOfLocationTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "locationTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfMaxHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHopCount");
          final int _cursorIndexOfDeliveryStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryStatus");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfCloudSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncStatus");
          final int _cursorIndexOfCloudSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncedAt");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final List<SosMessageEntity> _result = new ArrayList<SosMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SosMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpSenderDeviceId;
            _tmpSenderDeviceId = _cursor.getString(_cursorIndexOfSenderDeviceId);
            final String _tmpEmergencyCategory;
            _tmpEmergencyCategory = _cursor.getString(_cursorIndexOfEmergencyCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAssistanceTypes;
            _tmpAssistanceTypes = _cursor.getString(_cursorIndexOfAssistanceTypes);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpLocationAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfLocationAccuracyMeters)) {
              _tmpLocationAccuracyMeters = null;
            } else {
              _tmpLocationAccuracyMeters = _cursor.getFloat(_cursorIndexOfLocationAccuracyMeters);
            }
            final Long _tmpLocationTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocationTimestamp)) {
              _tmpLocationTimestamp = null;
            } else {
              _tmpLocationTimestamp = _cursor.getLong(_cursorIndexOfLocationTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpMaxHopCount;
            _tmpMaxHopCount = _cursor.getInt(_cursorIndexOfMaxHopCount);
            final String _tmpDeliveryStatus;
            _tmpDeliveryStatus = _cursor.getString(_cursorIndexOfDeliveryStatus);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpCloudSyncStatus;
            _tmpCloudSyncStatus = _cursor.getString(_cursorIndexOfCloudSyncStatus);
            final Long _tmpCloudSyncedAt;
            if (_cursor.isNull(_cursorIndexOfCloudSyncedAt)) {
              _tmpCloudSyncedAt = null;
            } else {
              _tmpCloudSyncedAt = _cursor.getLong(_cursorIndexOfCloudSyncedAt);
            }
            final String _tmpIncidentId;
            if (_cursor.isNull(_cursorIndexOfIncidentId)) {
              _tmpIncidentId = null;
            } else {
              _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            }
            _item = new SosMessageEntity(_tmpId,_tmpMessageType,_tmpSenderDeviceId,_tmpEmergencyCategory,_tmpDescription,_tmpAssistanceTypes,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracyMeters,_tmpLocationTimestamp,_tmpCreatedAt,_tmpExpiresAt,_tmpHopCount,_tmpMaxHopCount,_tmpDeliveryStatus,_tmpDirection,_tmpCloudSyncStatus,_tmpCloudSyncedAt,_tmpIncidentId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingCloudSyncMessages(
      final Continuation<? super List<SosMessageEntity>> $completion) {
    final String _sql = "SELECT * FROM sos_messages WHERE cloudSyncStatus = 'PENDING' OR cloudSyncStatus = 'FAILED' ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SosMessageEntity>>() {
      @Override
      @NonNull
      public List<SosMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSenderDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDeviceId");
          final int _cursorIndexOfEmergencyCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyCategory");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAssistanceTypes = CursorUtil.getColumnIndexOrThrow(_cursor, "assistanceTypes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "locationAccuracyMeters");
          final int _cursorIndexOfLocationTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "locationTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfMaxHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHopCount");
          final int _cursorIndexOfDeliveryStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryStatus");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfCloudSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncStatus");
          final int _cursorIndexOfCloudSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cloudSyncedAt");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final List<SosMessageEntity> _result = new ArrayList<SosMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SosMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpSenderDeviceId;
            _tmpSenderDeviceId = _cursor.getString(_cursorIndexOfSenderDeviceId);
            final String _tmpEmergencyCategory;
            _tmpEmergencyCategory = _cursor.getString(_cursorIndexOfEmergencyCategory);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAssistanceTypes;
            _tmpAssistanceTypes = _cursor.getString(_cursorIndexOfAssistanceTypes);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpLocationAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfLocationAccuracyMeters)) {
              _tmpLocationAccuracyMeters = null;
            } else {
              _tmpLocationAccuracyMeters = _cursor.getFloat(_cursorIndexOfLocationAccuracyMeters);
            }
            final Long _tmpLocationTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocationTimestamp)) {
              _tmpLocationTimestamp = null;
            } else {
              _tmpLocationTimestamp = _cursor.getLong(_cursorIndexOfLocationTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpMaxHopCount;
            _tmpMaxHopCount = _cursor.getInt(_cursorIndexOfMaxHopCount);
            final String _tmpDeliveryStatus;
            _tmpDeliveryStatus = _cursor.getString(_cursorIndexOfDeliveryStatus);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpCloudSyncStatus;
            _tmpCloudSyncStatus = _cursor.getString(_cursorIndexOfCloudSyncStatus);
            final Long _tmpCloudSyncedAt;
            if (_cursor.isNull(_cursorIndexOfCloudSyncedAt)) {
              _tmpCloudSyncedAt = null;
            } else {
              _tmpCloudSyncedAt = _cursor.getLong(_cursorIndexOfCloudSyncedAt);
            }
            final String _tmpIncidentId;
            if (_cursor.isNull(_cursorIndexOfIncidentId)) {
              _tmpIncidentId = null;
            } else {
              _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            }
            _item = new SosMessageEntity(_tmpId,_tmpMessageType,_tmpSenderDeviceId,_tmpEmergencyCategory,_tmpDescription,_tmpAssistanceTypes,_tmpLatitude,_tmpLongitude,_tmpLocationAccuracyMeters,_tmpLocationTimestamp,_tmpCreatedAt,_tmpExpiresAt,_tmpHopCount,_tmpMaxHopCount,_tmpDeliveryStatus,_tmpDirection,_tmpCloudSyncStatus,_tmpCloudSyncedAt,_tmpIncidentId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getPendingCloudSyncCount() {
    final String _sql = "SELECT COUNT(*) FROM sos_messages WHERE cloudSyncStatus = 'PENDING' OR cloudSyncStatus = 'FAILED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sos_messages"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getSyncedCloudCount() {
    final String _sql = "SELECT COUNT(*) FROM sos_messages WHERE cloudSyncStatus = 'SYNCED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sos_messages"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getMessageCount() {
    final String _sql = "SELECT COUNT(*) FROM sos_messages";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sos_messages"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getReceivedCount() {
    final String _sql = "SELECT COUNT(*) FROM sos_messages WHERE direction = 'INCOMING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sos_messages"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object messageExists(final String messageId,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM sos_messages WHERE id = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, messageId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
