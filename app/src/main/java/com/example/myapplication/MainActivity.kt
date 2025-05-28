package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.view.Gravity
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.Fuel
import com.example.myapplication.db.Users
import com.example.myapplication.db.Warehouse
import DB_Func
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.textfield.TextInputLayout
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.View

class MainActivity() : AppCompatActivity(), Parcelable {
    val db_func = DB_Func()

    private lateinit var textView: TextView
    private lateinit var button: Button
    var currentUserId: Int = 0
    private var currentUserRole: String = ""
    val adminUser = Users(
        login = "admin",
        password = "admin",
        role = "admin"
    )

    val fueloperation = Fuel(
        fuel_type = "ДТ",
        operation_type = "Прихід",
        quantity_litr = "1500",
        operatorID = 1,
        plomba = "777",
        created_at = "21.05.2025"
    )
    private lateinit var buttonn2: Button
    private lateinit var loginEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var db: AppDatabase
    private lateinit var buttonAddGasOperation: Button

    constructor(parcel: Parcel) : this() {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "my-db"
            ).fallbackToDestructiveMigration()
            .build()
            Log.d("DB","Removed")

            setContentView(R.layout.login_layout)
            enableEdgeToEdge()
            Log.d("login_layout", "onCreate called")

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            button = findViewById(R.id.button3)
            loginEditText = findViewById(R.id.EditTextLogin)
            passwordEditText = findViewById(R.id.EditTextPassword)

            // Додаємо адміністратора при першому запуску
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Перевіряємо чи існує вже адміністратор
                    val existingAdmin = db.UsersDAO().doUserIsCreated("admin", "admin")
                    
                    if (existingAdmin == null) {
                        // Створюємо об'єкт Users тільки якщо адміна ще немає
                        val adminUser = Users(
                            login = "admin",
                            password = "admin",
                            role = "admin"
                        )
                        
                        // Додаємо його в базу даних
                        db.UsersDAO().insertAll(adminUser)
                        db.FuelDAO().insertAll(fueloperation)
                        Log.d("Database", "Admin user created successfully")
                    } else {
                        Log.d("Database", "Admin user already exists")
                    }
                } catch (e: Exception) {
                    Log.e("Database", "Error creating admin user", e)
                }
            }

            button.setOnClickListener {
                val loginPut = loginEditText.text?.toString()
                val passinPut = passwordEditText.text?.toString()

                Log.d("Login", "Login button clicked")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (!loginPut.isNullOrBlank() && !passinPut.isNullOrBlank()) {
                            val user = db.UsersDAO().doUserIsCreated(loginPut, passinPut)

                            launch(Dispatchers.Main) {
                                Log.d("FuelLayout", "Checking user")
                                if (user != null) {
                                    Log.d("FuelLayout", "User role: ${user.role}")
                                    currentUserId = user.id
                                    currentUserRole = user.role
                                    when (user.role) {
                                        "tanker" -> {
                                            setContentView(R.layout.gas_layout)
                                            initTankerLayout()
                                            button = findViewById(R.id.addOperationBatton)
                                        }
                                        "admin" -> {
                                            setContentView(R.layout.admin_panel)
                                            initAdminPanel()
                                        }
                                        "storekeeper" -> {
                                            setContentView(R.layout.warhouses_info)
                                            initWarehouseLayout()
                                        }
                                        "CEO" -> {
                                            setContentView(R.layout.ceo_panel)
                                            initCeoPanel()
                                        }
                                    }
                                } else {
                                    Toast.makeText(this@MainActivity, "Невірний логін або пароль", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            launch(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Будь ласка, введіть логін і пароль", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Database", "Error in database operation", e)
                        launch(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Помилка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
            Toast.makeText(this, "Помилка ініціалізації: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initTankerLayout() {
        try {
            // Додаємо кнопку повернення для CEO
            if (currentUserRole == "CEO") {
                val backButton = ImageButton(this).apply {
                    setImageResource(android.R.drawable.ic_menu_revert)
                    layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                        startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        marginStart = 16
                        bottomMargin = 16
                    }
                }
                findViewById<ConstraintLayout>(R.id.main)?.addView(backButton)
                
                backButton.setOnClickListener {
                    returnToCeoPanel()
                }
            }

            val fuelTable = findViewById<TableLayout>(R.id.FuelTable)
            val buttonAddGasOperation = findViewById<Button>(R.id.addOperationBatton)

            // Приховуємо кнопку додавання для CEO
            if (currentUserRole == "CEO") {
                buttonAddGasOperation.visibility = View.GONE
            }

            // Очищаємо таблицю перед додаванням нових даних
            fuelTable.removeAllViews()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val fuelList = db.FuelDAO().getAll()
                    Log.d("FuelTable", "Отримано ${fuelList.size} записів")

                    withContext(Dispatchers.Main) {
                        fun makeCell(text: String, isHeader: Boolean = false): TextView {
                            return TextView(this@MainActivity).apply {
                                this.text = text
                                this.setPadding(8, 8, 8, 8)
                                this.textSize = 12f
                                if (isHeader) {
                                    this.setTypeface(null, Typeface.BOLD)
                                    this.setBackgroundResource(android.R.color.darker_gray)
                                    this.setTextColor(Color.WHITE)
                                } else {
                                    this.setBackgroundResource(android.R.drawable.editbox_background)
                                }
                                this.gravity = Gravity.CENTER
                                this.maxLines = 2
                                this.ellipsize = TextUtils.TruncateAt.END
                            }
                        }

                        // Створюємо заголовок
                        val headerRow = TableRow(this@MainActivity).apply {
                            layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                        }
                        
                        val headers = listOf(
                            "ID" to 40,
                            "Тип" to 80,
                            "Операція" to 80,
                            "Кількість" to 80,
                            "Оператор" to 60,
                            "Пломба" to 60,
                            "Дата" to 100
                        )

                        headers.forEach { (text, width) ->
                            val cell = makeCell(text, true)
                            cell.layoutParams = TableRow.LayoutParams().apply {
                                this.width = dpToPx(width)
                            }
                            headerRow.addView(cell)
                        }
                        fuelTable.addView(headerRow)

                        // Додаємо дані
                        fuelList.forEach { fuel ->
                            val row = TableRow(this@MainActivity)
                            val cells = listOf(
                                fuel.id.toString() to 40,
                                fuel.fuel_type to 80,
                                fuel.operation_type to 80,
                                fuel.quantity_litr to 80,
                                fuel.operatorID.toString() to 60,
                                fuel.plomba to 60,
                                fuel.created_at to 100
                            )

                            cells.forEach { (text, width) ->
                                val cell = makeCell(text)
                                cell.layoutParams = TableRow.LayoutParams().apply {
                                    this.width = dpToPx(width)
                                }
                                row.addView(cell)
                            }
                            fuelTable.addView(row)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FuelTable", "Помилка при завантаженні даних", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Помилка завантаження даних: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            buttonAddGasOperation.setOnClickListener {
                // Зберігаємо поточний стан перед переходом на новий екран
                setContentView(R.layout.gas_adding)
                val spinner: Spinner = findViewById(R.id.usersTypeSpinner)
                val spinner2: Spinner = findViewById(R.id.operationTypeSpinner)
                
                // Додаємо обробник для кнопки повернення
                val backButton = findViewById<android.widget.ImageButton>(R.id.imageButtonBack)
                backButton.setOnClickListener {
                    if (currentUserRole == "CEO") {
                        returnToCeoPanel()
                    } else {
                        returnToFuelTable()
                    }
                }

                // Дані для випадаючого списку
                val items = listOf("Бензин", "Дизель", "Газ", "Керосин")
                val items2 = listOf("Видача", "Прихід")

                // Адаптер для відображення даних у Spinner
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
                val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, items2)

                // Додаємо стиль для випадаючого списку
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                // Прив'язуємо адаптер до Spinner
                spinner.adapter = adapter
                spinner2.adapter = adapter2

                val submitOperationFuelButton = findViewById<Button>(R.id.submitButtonUsers)
                val fuelValLayout = findViewById<TextInputLayout>(R.id.fuelValEditText)
                val plombNumLayout = findViewById<TextInputLayout>(R.id.plombNumEditText)

                submitOperationFuelButton.setOnClickListener {
                    try {
                        val fuelTypeInput = spinner.selectedItem?.toString() ?: ""
                        val operationTypeInput = spinner2.selectedItem?.toString() ?: ""
                        val fuelVal = fuelValLayout.editText?.text?.toString() ?: ""
                        val plombNum = plombNumLayout.editText?.text?.toString() ?: ""

                        if (fuelTypeInput.isEmpty() || operationTypeInput.isEmpty() || 
                            fuelVal.isEmpty() || plombNum.isEmpty()) {
                            Toast.makeText(this@MainActivity, 
                                "Будь ласка, заповніть всі поля", 
                                Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val calendar = Calendar.getInstance()
                        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val currentTime = format.format(calendar.time)

                        val fueloperation = Fuel(
                            fuel_type = fuelTypeInput,
                            operation_type = operationTypeInput,
                            quantity_litr = fuelVal,
                            operatorID = currentUserId,
                            plomba = plombNum,
                            created_at = currentTime
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                db.FuelDAO().insertAll(fueloperation)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, 
                                        "Операцію успішно додано", 
                                        Toast.LENGTH_SHORT).show()
                                    returnToFuelTable()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, 
                                        "Помилка: ${e.localizedMessage}", 
                                        Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, 
                            "Помилка: ${e.localizedMessage}", 
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TankerLayout", "Error in initTankerLayout", e)
            Toast.makeText(this, "Помилка ініціалізації: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Функція для оновлення даних таблиці
    private fun refreshFuelTable() {
        initTankerLayout()
    }

    // Функція для повернення до таблиці після додавання
    private fun returnToFuelTable() {
        setContentView(R.layout.gas_layout)
        initTankerLayout()
    }

    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun initAdminPanel() {
        try {
            // Додаємо кнопку повернення для CEO
            if (currentUserRole == "CEO") {
                val backButton = ImageButton(this).apply {
                    setImageResource(android.R.drawable.ic_menu_revert)
                    layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                        startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        marginStart = 16
                        bottomMargin = 16
                    }
                }
                findViewById<ConstraintLayout>(R.id.main)?.addView(backButton)
                
                backButton.setOnClickListener {
                    returnToCeoPanel()
                }
            }

            val usersTable = findViewById<TableLayout>(R.id.UsersTable)
            val buttonAddUser = findViewById<Button>(R.id.addUserButton)

            // Приховуємо кнопку додавання для CEO
            if (currentUserRole == "CEO") {
                buttonAddUser.visibility = View.GONE
            }

            // Отримуємо ширину екрану
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            // Очищаємо таблицю перед додаванням нових даних
            usersTable.removeAllViews()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val usersList = db.UsersDAO().getAll()
                    Log.d("UsersTable", "Отримано ${usersList.size} записів")

                    withContext(Dispatchers.Main) {
                        fun makeCell(text: String, isHeader: Boolean = false, widthPercent: Float): TextView {
                            return TextView(this@MainActivity).apply {
                                this.text = text
                                this.setPadding(8, 8, 8, 8)
                                this.textSize = 12f
                                if (isHeader) {
                                    this.setTypeface(null, Typeface.BOLD)
                                    this.setBackgroundResource(android.R.color.darker_gray)
                                    this.setTextColor(Color.WHITE)
                                } else {
                                    this.setBackgroundResource(android.R.drawable.editbox_background)
                                }
                                this.gravity = Gravity.CENTER
                                this.maxLines = 2
                                this.ellipsize = TextUtils.TruncateAt.END
                                
                                // Встановлюємо ширину як відсоток від ширини екрану
                                val cellWidth = (screenWidth * widthPercent).toInt()
                                this.layoutParams = TableRow.LayoutParams().apply {
                                    width = cellWidth
                                }
                            }
                        }

                        // Створюємо заголовок
                        val headerRow = TableRow(this@MainActivity).apply {
                            layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                        }
                        
                        // Визначаємо ширину кожного стовпця у відсотках
                        val headers = listOf(
                            "ID" to 0.1f,          // 10% ширини
                            "Логін" to 0.3f,       // 30% ширини
                            "Пароль" to 0.3f,      // 30% ширини
                            "Роль" to 0.3f         // 30% ширини
                        )

                        headers.forEach { (text, widthPercent) ->
                            headerRow.addView(makeCell(text, true, widthPercent))
                        }
                        usersTable.addView(headerRow)

                        // Додаємо дані користувачів
                        usersList.forEach { user ->
                            val row = TableRow(this@MainActivity)
                            val cells = listOf(
                                user.id.toString() to 0.1f,
                                user.login to 0.3f,
                                user.password to 0.3f,
                                user.role to 0.3f
                            )

                            cells.forEach { (text, widthPercent) ->
                                row.addView(makeCell(text, false, widthPercent))
                            }
                            usersTable.addView(row)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UsersTable", "Помилка при завантаженні даних", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Помилка завантаження даних: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            buttonAddUser.setOnClickListener {
                setContentView(R.layout.adding_user)
                
                val loginInput = findViewById<TextInputEditText>(R.id.loginValEditTextInput)
                val passwordInput = findViewById<TextInputEditText>(R.id.passwordEditTextInput)
                val roleSpinner = findViewById<Spinner>(R.id.usersTypeSpinner)
                val submitButton = findViewById<Button>(R.id.submitButtonUsers)
                val backButton = findViewById<ImageButton>(R.id.imageButtonBack)

                // Налаштовуємо спіннер для ролей
                val roles = listOf("tanker", "storekeeper", "CEO", "admin")
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                roleSpinner.adapter = adapter

                // Обробник кнопки повернення
                backButton.setOnClickListener {
                    if (currentUserRole == "CEO") {
                        returnToCeoPanel()
                    } else {
                        returnToAdminPanel()
                    }
                }

                // Обробник кнопки створення користувача
                submitButton.setOnClickListener {
                    val login = loginInput.text?.toString()
                    val password = passwordInput.text?.toString()
                    val role = roleSpinner.selectedItem?.toString()

                    if (login.isNullOrEmpty() || password.isNullOrEmpty() || role.isNullOrEmpty()) {
                        Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Перевіряємо чи існує користувач з таким логіном
                            val existingUser = db.UsersDAO().getUserByLogin(login)
                            if (existingUser != null) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, 
                                        "Користувач з таким логіном вже існує", 
                                        Toast.LENGTH_SHORT).show()
                                }
                                return@launch
                            }

                            // Створюємо нового користувача
                            val newUser = Users(
                                login = login,
                                password = password,
                                role = role
                            )
                            db.UsersDAO().insertAll(newUser)

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, 
                                    "Користувача успішно створено", 
                                    Toast.LENGTH_SHORT).show()
                                if (currentUserRole == "CEO") {
                                    returnToCeoPanel()
                                } else {
                                    returnToAdminPanel()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, 
                                    "Помилка: ${e.localizedMessage}", 
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AdminPanel", "Error in initAdminPanel", e)
            Toast.makeText(this, "Помилка ініціалізації: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun returnToAdminPanel() {
        setContentView(R.layout.admin_panel)
        initAdminPanel()
    }

    private fun initWarehouseLayout() {
        try {
            val warehouseTable = findViewById<TableLayout>(R.id.FuelTable)
            val buttonAddWarehouseOperation = findViewById<Button>(R.id.addWarhouseOperationButton)

            // Приховуємо кнопку додавання для CEO
            if (currentUserRole == "CEO") {
                buttonAddWarehouseOperation.visibility = View.GONE
            }

            // Додаємо кнопку повернення для CEO
            if (currentUserRole == "CEO") {
                val backButton = ImageButton(this).apply {
                    setImageResource(android.R.drawable.ic_menu_revert)
                    layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                        startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        marginStart = 16
                        bottomMargin = 16
                    }
                }
                findViewById<ConstraintLayout>(R.id.main)?.addView(backButton)
                
                backButton.setOnClickListener {
                    returnToCeoPanel()
                }
            }

            // Очищаємо таблицю перед додаванням нових даних
            warehouseTable.removeAllViews()

            // Додаємо лог для перевірки
            Log.d("WarehouseLayout", "Initializing warehouse layout")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val warehouseList = db.WarehouseDAO().getAll()
                    Log.d("WarehouseTable", "Отримано ${warehouseList.size} записів")

                    withContext(Dispatchers.Main) {
                        fun makeCell(text: String, isHeader: Boolean = false): TextView {
                            return TextView(this@MainActivity).apply {
                                this.text = text
                                this.setPadding(8, 8, 8, 8)
                                this.textSize = 12f
                                if (isHeader) {
                                    this.setTypeface(null, Typeface.BOLD)
                                    this.setBackgroundResource(android.R.color.darker_gray)
                                    this.setTextColor(Color.WHITE)
                                } else {
                                    this.setBackgroundResource(android.R.drawable.editbox_background)
                                }
                                this.gravity = Gravity.CENTER
                                this.maxLines = 2
                                this.ellipsize = TextUtils.TruncateAt.END
                            }
                        }

                        // Створюємо заголовок
                        val headerRow = TableRow(this@MainActivity).apply {
                            layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                        }
                        
                        val headers = listOf(
                            "ID" to 40,
                            "Продукт" to 80,
                            "Операція" to 80,
                            "Кількість" to 60,
                            "Кому видано" to 100,
                            "№ складу" to 60,
                            "Дата" to 100
                        )

                        headers.forEach { (text, width) ->
                            val cell = makeCell(text, true)
                            cell.layoutParams = TableRow.LayoutParams().apply {
                                this.width = dpToPx(width)
                            }
                            headerRow.addView(cell)
                        }
                        warehouseTable.addView(headerRow)

                        // Додаємо дані
                        warehouseList.forEach { warehouse ->
                            val row = TableRow(this@MainActivity)
                            val cells = listOf(
                                warehouse.id.toString() to 40,
                                warehouse.product_type to 80,
                                warehouse.operation_type to 80,
                                warehouse.quantity_kg to 60,
                                warehouse.issued_to to 100,
                                warehouse.warehouse_number to 60,
                                warehouse.created_at to 100
                            )

                            cells.forEach { (text, width) ->
                                val cell = makeCell(text)
                                cell.layoutParams = TableRow.LayoutParams().apply {
                                    this.width = dpToPx(width)
                                }
                                row.addView(cell)
                            }
                            warehouseTable.addView(row)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WarehouseTable", "Помилка при завантаженні даних", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Помилка завантаження даних: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            // Додаємо лог перед встановленням обробника кліку
            Log.d("WarehouseLayout", "Setting up button click listener")

            buttonAddWarehouseOperation.setOnClickListener {
                Log.d("WarehouseLayout", "Add button clicked")
                setContentView(R.layout.adding_warhouse_operation)
                
                val spinner: Spinner = findViewById(R.id.usersTypeSpinner)
                val spinner2: Spinner = findViewById(R.id.operationTypeSpinner)
                val backButton = findViewById<ImageButton>(R.id.imageButtonBack)
                
                // Дані для випадаючих списків
                val products = listOf("Пшениця", "Жито", "Ячмінь", "Овес", "Кукурудза")
                val operations = listOf("Видача", "Прихід")

                // Адаптери для спіннерів
                val productAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, products)
                val operationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, operations)

                productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                operationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                spinner.adapter = productAdapter
                spinner2.adapter = operationAdapter

                // Кнопка повернення
                backButton.setOnClickListener {
                    if (currentUserRole == "CEO") {
                        returnToCeoPanel()
                    } else {
                        returnToWarehouseTable()
                    }
                }

                // Обробка додавання нового запису
                val submitButton = findViewById<Button>(R.id.submitButtonUsers)
                submitButton.setOnClickListener {
                    val productType = spinner.selectedItem?.toString() ?: ""
                    val operationType = spinner2.selectedItem?.toString() ?: ""
                    val quantity = findViewById<TextInputLayout>(R.id.fuelValEditText).editText?.text?.toString() ?: ""
                    val issuedTo = findViewById<TextInputLayout>(R.id.plombNumEditText).editText?.text?.toString() ?: ""
                    val warehouseNumber = findViewById<TextInputLayout>(R.id.textInputLayout3).editText?.text?.toString() ?: ""

                    if (productType.isEmpty() || operationType.isEmpty() || quantity.isEmpty() || 
                        issuedTo.isEmpty() || warehouseNumber.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val calendar = Calendar.getInstance()
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val currentTime = format.format(calendar.time)

                    val warehouse = Warehouse(
                        product_type = productType,
                        operation_type = operationType,
                        quantity_kg = quantity,
                        issued_to = issuedTo,
                        warehouse_number = warehouseNumber,
                        operatorID = currentUserId,
                        created_at = currentTime
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            db.WarehouseDAO().insertAll(warehouse)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Операцію успішно додано", Toast.LENGTH_SHORT).show()
                                returnToWarehouseTable()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Помилка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WarehouseLayout", "Error in initWarehouseLayout", e)
            Toast.makeText(this, "Помилка ініціалізації: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun returnToWarehouseTable() {
        setContentView(R.layout.warhouses_info)
        initWarehouseLayout()
    }

    private fun initCeoPanel() {
        try {
            Log.d("CeoPanel", "Initializing CEO panel")
            
            // Ініціалізація кнопок
            val warehousesButton = findViewById<Button>(R.id.button4)
            val usersButton = findViewById<Button>(R.id.button5)
            val fuelButton = findViewById<Button>(R.id.button6)

            // Обробник для кнопки складів
            warehousesButton.setOnClickListener {
                Log.d("CeoPanel", "Warehouses button clicked")
                setContentView(R.layout.warhouses_info)
                initWarehouseLayout()
            }

            // Обробник для кнопки користувачів
            usersButton.setOnClickListener {
                Log.d("CeoPanel", "Users button clicked")
                setContentView(R.layout.admin_panel)
                initAdminPanel()
            }

            // Обробник для кнопки палива
            fuelButton.setOnClickListener {
                Log.d("CeoPanel", "Fuel button clicked")
                setContentView(R.layout.gas_layout)
                initTankerLayout()
            }

        } catch (e: Exception) {
            Log.e("CeoPanel", "Error initializing CEO panel", e)
            Toast.makeText(this, "Помилка ініціалізації панелі: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Функція для повернення до CEO панелі
    private fun returnToCeoPanel() {
        setContentView(R.layout.ceo_panel)
        initCeoPanel()
    }
}