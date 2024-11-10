import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "complaints.db"
        private const val DATABASE_VERSION = 1

        // Таблица пользователей (User)
        private const val TABLE_USER = "User"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_LAST_NAME = "lastName"
        private const val COLUMN_FIRST_NAME = "firstName"
        private const val COLUMN_MIDDLE_NAME = "middleName"

        // Таблица жалоб (Complaint)
        private const val TABLE_COMPLAINT = "Complaint"
        private const val COLUMN_COMPLAINT_ID = "id"
        private const val COLUMN_USER_ID_FK = "userId"
        private const val COLUMN_HOUSING_TYPE = "housingType"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_COMPLAINT_TEXT = "complaintText"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """CREATE TABLE $TABLE_USER (
            $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_LAST_NAME TEXT,
            $COLUMN_FIRST_NAME TEXT,
            $COLUMN_MIDDLE_NAME TEXT
        )"""

        val createComplaintTable = """CREATE TABLE $TABLE_COMPLAINT (
            $COLUMN_COMPLAINT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_ID_FK INTEGER,
            $COLUMN_HOUSING_TYPE TEXT,
            $COLUMN_ADDRESS TEXT,
            $COLUMN_COMPLAINT_TEXT TEXT,
            FOREIGN KEY ($COLUMN_USER_ID_FK) REFERENCES $TABLE_USER($COLUMN_USER_ID)
        )"""

        db.execSQL(createUserTable)
        db.execSQL(createComplaintTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPLAINT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // Метод для добавления пользователя
    fun addUser(lastName: String, firstName: String, middleName: String?): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_MIDDLE_NAME, middleName)
        }
        return db.insert(TABLE_USER, null, values)
    }

    // Метод для добавления жалобы
    fun addComplaint(complaint: Complaint, userId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID_FK, userId)  // Используем правильный userId
            put(COLUMN_HOUSING_TYPE, complaint.housingType)
            put(COLUMN_ADDRESS, complaint.address)
            put(COLUMN_COMPLAINT_TEXT, complaint.complaintText)
        }
        return db.insert(TABLE_COMPLAINT, null, values)  // Возвращает новый id или -1 в случае ошибки
    }



    // Метод для получения всех жалоб
    fun getAllComplaints(): List<Complaint> {
        val complaints = mutableListOf<Complaint>()
        val db = readableDatabase

        // Используем JOIN для объединения таблиц User и Complaint
        val cursor: Cursor = db.rawQuery("""
        SELECT c.*, u.$COLUMN_LAST_NAME, u.$COLUMN_FIRST_NAME, u.$COLUMN_MIDDLE_NAME 
        FROM $TABLE_COMPLAINT c
        INNER JOIN $TABLE_USER u ON c.$COLUMN_USER_ID_FK = u.$COLUMN_USER_ID
    """, null)

        if (cursor.moveToFirst()) {
            do {
                val complaint = Complaint(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_ID)), // id
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)), // lastName из таблицы User
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)), // firstName из таблицы User
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME)), // middleName из таблицы User
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSING_TYPE)), // housingType
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)), // address
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_TEXT)) // complaintText
                )
                complaints.add(complaint)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return complaints
    }


    // Метод для удаления жалобы
    fun deleteComplaint(complaintId: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(TABLE_COMPLAINT, "$COLUMN_COMPLAINT_ID = ?", arrayOf(complaintId.toString()))
        return rowsDeleted > 0
    }

    // Метод для обновления жалобы
    fun updateComplaint(complaint: Complaint): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_HOUSING_TYPE, complaint.housingType)
            put(COLUMN_ADDRESS, complaint.address)
            put(COLUMN_COMPLAINT_TEXT, complaint.complaintText)
        }
        val rowsUpdated = db.update(TABLE_COMPLAINT, values, "$COLUMN_COMPLAINT_ID = ?", arrayOf(complaint.id.toString()))
        return rowsUpdated > 0
    }

}
