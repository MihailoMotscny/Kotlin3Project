import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Seeds::class, Users::class, Fuel::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun seedsDao(): DAO
    abstract fun UsersDAO(): UsersDAAO
    abstract fun FuelDAO(): FuelDAO
}

