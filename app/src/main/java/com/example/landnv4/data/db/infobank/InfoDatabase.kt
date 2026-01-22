package com.example.landnv4.data.db.infobank





/*package com.example.landnv4.data.db.infobank

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        AnchoringPointEntity::class,
        NorthingPointEntity::class,
        ValidatingPointEntity::class,
        TargetEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class InfoDatabase : RoomDatabase() {
    abstract fun pointsDao(): PointsDao

    companion object {
        @Volatile private var INSTANCE: InfoDatabase? = null

        fun getInstance(context: Context): InfoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    InfoDatabase::class.java,
                    "northing_db"
                ).build().also { INSTANCE = it }
            }
    }
}
*/