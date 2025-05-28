class DB_Func {
    fun parseSeedsFromString(data: String): Seeds? {
        val parts = data.split(",")
        return if (parts.size == 4) {
            Seeds(
                variety = parts[0],
                quantity_kg = parts[1],
                warehouse_id = parts[2].toInt(),
                created_at = parts[3]
            )
        } else {
            null // повертаємо null, якщо дані некоректні
        }
    }

    fun parseUsersFromString(data: String): Users? {
        val parts = data.split(",")
        return if (parts.size == 3) {
            Users(
                login = parts[0],
                password = parts[1],
                role = parts[2]
            )
        } else {
            null // повертаємо null, якщо дані некоректні
        }
    }

    fun parseFuelOperationFromString(data: String): Fuel? {
        val parts = data.split(",")
        return if (parts.size == 6) {
            Fuel(
                fuel_type=parts[0],
                operation_type = parts[1],
                quantity_litr = parts[2],
                operatorID = parts[3].toInt(),
                plomba = parts[4],
                created_at = parts[5]
            )
        } else {
            null // повертаємо null, якщо дані некоректні
        }
    }
}