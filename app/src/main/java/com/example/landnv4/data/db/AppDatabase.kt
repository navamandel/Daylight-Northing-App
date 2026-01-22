package com.example.landnv4.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.landnv4.data.db.infobank.*
import com.example.landnv4.data.db.stars.*

@Database(
    entities = [
        // stars
        StarEntity::class,

        // infobank
        AnchoringPointEntity::class,
        NorthingPointEntity::class,
        ValidatingPointEntity::class,
        TargetEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class) // <-- same converters you used in InfoDatabase
abstract class AppDatabase : RoomDatabase() {

    abstract fun starDao(): StarDao
    abstract fun pointsDao(): PointsDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "northing_db"
                )
                    // If you’re still iterating and don’t care about preserving DB on updates:
                    // .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}




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

/*package com.example.landnv4.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.landnv4.data.db.stars.StarDao
import com.example.landnv4.data.db.stars.StarEntity

@Database(
    entities = [StarEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun starDao(): StarDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "northing_db"
                )
                    // remove this later when you have real migrations
                    // .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
*/