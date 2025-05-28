import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seeds")
data class Seeds(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val variety: String,
    val quantity_kg: String,
    val warehouse_id: Int,
    val created_at: String
)

@Entity(tableName = "users")
data class Users(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val login: String,
    val password: String,
    val role: String
)

@Entity(tableName = "fuel")
data class Fuel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fuel_type: String,
    val operation_type: String,
    val quantity_litr: String,
    val operatorID: Int,
    val plomba: String,
    val created_at: String
)