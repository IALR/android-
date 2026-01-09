package com.example.robotcontrol.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.robotcontrol.models.Robot;
import com.example.robotcontrol.models.RobotPermission;
import com.example.robotcontrol.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "RoboConnect.db";
    private static final int DATABASE_VERSION = 3;

    // Table names
    private static final String TABLE_ROBOTS = "robots";
    private static final String TABLE_CREDENTIALS = "credentials";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PERMISSIONS = "permissions";
    private static final String TABLE_LEARNING_PROGRESS = "learning_progress";

    // Robots table columns
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    private static final String KEY_IP_ADDRESS = "ip_address";
    private static final String KEY_TYPE = "type";
    private static final String KEY_OWNER_ID = "owner_id";
    private static final String KEY_CONNECTION_TYPE = "connection_type";
    private static final String KEY_LAST_CONNECTED = "last_connected";

    // Credentials table columns
    private static final String KEY_ROBOT_ID = "robot_id";
    private static final String KEY_SSID = "ssid";
    private static final String KEY_PASSWORD = "password";

    // Users table columns
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PASSWORD = "user_password";

    // Permissions table columns
    private static final String KEY_PERMISSION_ID = "permission_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_CAN_CONTROL = "can_control";
    private static final String KEY_GRANTED_AT = "granted_at";

    // Learning Progress table columns
    private static final String KEY_PROGRESS_ID = "progress_id";
    private static final String KEY_TOPIC = "topic";
    private static final String KEY_COMPLETED = "completed";
    private static final String KEY_SCORE = "score";
    private static final String KEY_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ROBOTS_TABLE = "CREATE TABLE " + TABLE_ROBOTS + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_MAC_ADDRESS + " TEXT,"
                + KEY_IP_ADDRESS + " TEXT,"
                + KEY_TYPE + " TEXT,"
                + KEY_OWNER_ID + " TEXT,"
                + KEY_CONNECTION_TYPE + " TEXT,"
                + KEY_LAST_CONNECTED + " INTEGER"
                + ")";

        String CREATE_CREDENTIALS_TABLE = "CREATE TABLE " + TABLE_CREDENTIALS + "("
                + KEY_ROBOT_ID + " TEXT PRIMARY KEY,"
                + KEY_SSID + " TEXT,"
                + KEY_PASSWORD + " TEXT"
                + ")";

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " TEXT PRIMARY KEY,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_USER_NAME + " TEXT,"
                + KEY_USER_PASSWORD + " TEXT"
                + ")";

        String CREATE_PERMISSIONS_TABLE = "CREATE TABLE " + TABLE_PERMISSIONS + "("
                + KEY_PERMISSION_ID + " TEXT PRIMARY KEY,"
                + KEY_ROBOT_ID + " TEXT,"
                + KEY_USER_ID + " TEXT,"
                + KEY_USER_EMAIL + " TEXT,"
                + KEY_CAN_CONTROL + " INTEGER,"
                + KEY_GRANTED_AT + " INTEGER"
                + ")";

        String CREATE_LEARNING_PROGRESS_TABLE = "CREATE TABLE " + TABLE_LEARNING_PROGRESS + "("
                + KEY_PROGRESS_ID + " TEXT PRIMARY KEY,"
                + KEY_USER_ID + " TEXT,"
                + KEY_TOPIC + " TEXT,"
                + KEY_COMPLETED + " INTEGER,"
                + KEY_SCORE + " INTEGER,"
                + KEY_TIMESTAMP + " INTEGER"
                + ")";

        db.execSQL(CREATE_ROBOTS_TABLE);
        db.execSQL(CREATE_CREDENTIALS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEARNING_PROGRESS);
        db.execSQL(CREATE_PERMISSIONS_TABLE);
        db.execSQL(CREATE_LEARNING_PROGRESS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROBOTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CREDENTIALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERMISSIONS);
        onCreate(db);
    }

    // Add robot
    public long addRobot(Robot robot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, robot.getId());
        values.put(KEY_NAME, robot.getName());
        values.put(KEY_MAC_ADDRESS, robot.getMacAddress());
        values.put(KEY_IP_ADDRESS, robot.getIpAddress());
        values.put(KEY_TYPE, robot.getType());
        values.put(KEY_OWNER_ID, robot.getOwnerId());
        values.put(KEY_CONNECTION_TYPE, robot.getConnectionType());
        values.put(KEY_LAST_CONNECTED, robot.getLastConnected());

        long result = db.insert(TABLE_ROBOTS, null, values);
        db.close();
        return result;
    }

    // Get robot by ID
    public Robot getRobot(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROBOTS, null, KEY_ID + "=?",
                new String[]{id}, null, null, null);

        Robot robot = null;
        if (cursor != null && cursor.moveToFirst()) {
            robot = new Robot();
            robot.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
            robot.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
            robot.setMacAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAC_ADDRESS)));
            robot.setIpAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IP_ADDRESS)));
            robot.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)));
            robot.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_OWNER_ID)));
            robot.setConnectionType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONNECTION_TYPE)));
            robot.setLastConnected(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_LAST_CONNECTED)));
            cursor.close();
        }
        db.close();
        return robot;
    }

    // Get all robots
    public List<Robot> getAllRobots() {
        List<Robot> robotList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ROBOTS + " ORDER BY " + KEY_LAST_CONNECTED + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Robot robot = new Robot();
                robot.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
                robot.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
                robot.setMacAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAC_ADDRESS)));
                robot.setIpAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IP_ADDRESS)));
                robot.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)));
                robot.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_OWNER_ID)));
                robot.setConnectionType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONNECTION_TYPE)));
                robot.setLastConnected(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_LAST_CONNECTED)));
                robotList.add(robot);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return robotList;
    }

    // Update robot
    public int updateRobot(Robot robot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, robot.getName());
        values.put(KEY_MAC_ADDRESS, robot.getMacAddress());
        values.put(KEY_IP_ADDRESS, robot.getIpAddress());
        values.put(KEY_TYPE, robot.getType());
        values.put(KEY_CONNECTION_TYPE, robot.getConnectionType());
        values.put(KEY_LAST_CONNECTED, robot.getLastConnected());

        int result = db.update(TABLE_ROBOTS, values, KEY_ID + "=?", new String[]{robot.getId()});
        db.close();
        return result;
    }

    // Delete robot
    public void deleteRobot(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROBOTS, KEY_ID + "=?", new String[]{id});
        db.delete(TABLE_CREDENTIALS, KEY_ROBOT_ID + "=?", new String[]{id});
        db.close();
    }

    // Save WiFi credentials
    public void saveCredentials(String robotId, String ssid, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROBOT_ID, robotId);
        values.put(KEY_SSID, ssid);
        values.put(KEY_PASSWORD, password);

        db.insertWithOnConflict(TABLE_CREDENTIALS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Get WiFi credentials
    public String[] getCredentials(String robotId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CREDENTIALS, new String[]{KEY_SSID, KEY_PASSWORD},
                KEY_ROBOT_ID + "=?", new String[]{robotId}, null, null, null);

        String[] credentials = null;
        if (cursor != null && cursor.moveToFirst()) {
            credentials = new String[2];
            credentials[0] = cursor.getString(0);
            credentials[1] = cursor.getString(1);
            cursor.close();
        }
        db.close();
        return credentials;
    }

    // ==================== USER METHODS ====================

    // Add user
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, user.getId());
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_USER_NAME, user.getName());
        values.put(KEY_USER_PASSWORD, user.getPassword());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    // Get user by email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, KEY_EMAIL + "=?",
                new String[]{email}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD)));
            cursor.close();
        }
        db.close();
        return user;
    }

    // Get user by ID
    public User getUserById(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, KEY_USER_ID + "=?",
                new String[]{userId}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD)));
            cursor.close();
        }
        db.close();
        return user;
    }

    // ==================== PERMISSION METHODS ====================

    // Add permission
    public long addPermission(RobotPermission permission) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String permissionId = UUID.randomUUID().toString();
        values.put(KEY_PERMISSION_ID, permissionId);
        values.put(KEY_ROBOT_ID, permission.getRobotId());
        values.put(KEY_USER_ID, permission.getUserId());
        values.put(KEY_USER_EMAIL, permission.getUserEmail());
        values.put(KEY_CAN_CONTROL, permission.isCanControl() ? 1 : 0);
        values.put(KEY_GRANTED_AT, permission.getGrantedAt());

        long result = db.insert(TABLE_PERMISSIONS, null, values);
        db.close();
        return result;
    }

    // Get permissions for a robot
    public List<RobotPermission> getPermissionsForRobot(String robotId) {
        List<RobotPermission> permissions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PERMISSIONS, null, KEY_ROBOT_ID + "=?",
                new String[]{robotId}, null, null, KEY_GRANTED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                RobotPermission permission = new RobotPermission();
                permission.setRobotId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROBOT_ID)));
                permission.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
                permission.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)));
                permission.setCanControl(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAN_CONTROL)) == 1);
                permission.setGrantedAt(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GRANTED_AT)));
                permissions.add(permission);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return permissions;
    }

    // Delete specific permission
    public void deletePermission(String robotId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PERMISSIONS, KEY_ROBOT_ID + "=? AND " + KEY_USER_ID + "=?",
                new String[]{robotId, userId});
        db.close();
    }

    // Delete all permissions for a robot
    public void deletePermissionsForRobot(String robotId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PERMISSIONS, KEY_ROBOT_ID + "=?", new String[]{robotId});
        db.close();
    }

    // Get robots shared with a user
    public List<Robot> getSharedRobots(String userId) {
        List<Robot> robots = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT r.* FROM " + TABLE_ROBOTS + " r " +
                "INNER JOIN " + TABLE_PERMISSIONS + " p ON r." + KEY_ID + " = p." + KEY_ROBOT_ID +
                " WHERE p." + KEY_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{userId});

        if (cursor.moveToFirst()) {
            do {
                Robot robot = new Robot();
                robot.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
                robot.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
                robot.setMacAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAC_ADDRESS)));
                robot.setIpAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IP_ADDRESS)));
                robot.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)));
                robot.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_OWNER_ID)));
                robot.setConnectionType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONNECTION_TYPE)));
                robot.setLastConnected(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_LAST_CONNECTED)));
                robots.add(robot);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return robots;
    }
}
