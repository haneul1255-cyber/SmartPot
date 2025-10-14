# 📚 SmartPot 데이터베이스 사용 가이드

## 📋 목차
1. [DBHelper 객체 생성](#1-dbhelper-객체-생성)
2. [화분(Plant) 관리](#2-화분plant-관리)
3. [센서 데이터 관리](#3-센서-데이터-관리)
4. [고급 쿼리](#4-고급-쿼리)
5. [실전 예제](#5-실전-예제)

---

## 1. DBHelper 객체 생성

### 기본 생성
```java
// Activity 또는 Service에서
DBHelper dbHelper = new DBHelper(this);
```

### 데이터베이스 열기
```java
// 읽기 전용
SQLiteDatabase readDb = dbHelper.getReadableDatabase();

// 읽기/쓰기 모드
SQLiteDatabase writeDb = dbHelper.getWritableDatabase();
```

### 데이터베이스 닫기 (중요!)
```java
// 사용 후 반드시 닫기
dbHelper.close();
```

---

## 2. 화분(Plant) 관리

### 📊 테이블 구조
```sql
plantTBL (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plantName TEXT NOT NULL,
    plantType TEXT NOT NULL,
    bluetoothAddress TEXT UNIQUE NOT NULL,
    bluetoothName TEXT,
    registeredDate INTEGER,
    wateringInterval INTEGER,
    isActive INTEGER DEFAULT 1
)
```

### ➕ 화분 추가 (INSERT)

```java
public long insertPlant(String plantName, String plantType, String bluetoothAddress, String bluetoothName) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    
    values.put("plantName", plantName);
    values.put("plantType", plantType);
    values.put("bluetoothAddress", bluetoothAddress);
    values.put("bluetoothName", bluetoothName);
    values.put("registeredDate", System.currentTimeMillis());
    values.put("wateringInterval", 7);  // 기본 7일
    values.put("isActive", 1);
    
    long newRowId = db.insert("plantTBL", null, values);
    db.close();
    
    return newRowId;  // 성공 시 새 행의 ID, 실패 시 -1
}
```

**간단한 사용 예시:**
```java
DBHelper dbHelper = new DBHelper(this);
long plantId = insertPlant("거실 몬스테라", "관엽식물", "98:D3:31:F5:A2:B1", "HC-06");

if (plantId != -1) {
    Toast.makeText(this, "화분 등록 성공! ID: " + plantId, Toast.LENGTH_SHORT).show();
} else {
    Toast.makeText(this, "화분 등록 실패", Toast.LENGTH_SHORT).show();
}
```

### 🔍 화분 조회 (SELECT)

#### 모든 화분 조회
```java
public List<Plant> getAllPlants() {
    List<Plant> plantList = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    
    Cursor cursor = db.query(
        "plantTBL",                      // 테이블명
        null,                            // 모든 컬럼 (null = SELECT *)
        "isActive = ?",                  // WHERE 조건
        new String[]{"1"},               // WHERE 값
        null,                            // GROUP BY
        null,                            // HAVING
        "id DESC"                        // ORDER BY (최신순)
    );
    
    while (cursor.moveToNext()) {
        Plant plant = new Plant();
        plant.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        plant.plantName = cursor.getString(cursor.getColumnIndexOrThrow("plantName"));
        plant.plantType = cursor.getString(cursor.getColumnIndexOrThrow("plantType"));
        plant.bluetoothAddress = cursor.getString(cursor.getColumnIndexOrThrow("bluetoothAddress"));
        plant.bluetoothName = cursor.getString(cursor.getColumnIndexOrThrow("bluetoothName"));
        plant.registeredDate = cursor.getLong(cursor.getColumnIndexOrThrow("registeredDate"));
        plant.wateringInterval = cursor.getInt(cursor.getColumnIndexOrThrow("wateringInterval"));
        
        plantList.add(plant);
    }
    
    cursor.close();
    db.close();
    return plantList;
}
```

#### 특정 화분 조회 (ID로)
```java
public Plant getPlantById(int plantId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Plant plant = null;
    
    Cursor cursor = db.query(
        "plantTBL",
        null,
        "id = ?",
        new String[]{String.valueOf(plantId)},
        null, null, null
    );
    
    if (cursor.moveToFirst()) {
        plant = new Plant();
        plant.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        plant.plantName = cursor.getString(cursor.getColumnIndexOrThrow("plantName"));
        plant.plantType = cursor.getString(cursor.getColumnIndexOrThrow("plantType"));
        plant.bluetoothAddress = cursor.getString(cursor.getColumnIndexOrThrow("bluetoothAddress"));
        // ... 나머지 필드
    }
    
    cursor.close();
    db.close();
    return plant;
}
```

#### 블루투스 주소로 화분 찾기 (중요!)
```java
public int getPlantIdByBluetoothAddress(String bluetoothAddress) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    int plantId = -1;
    
    Cursor cursor = db.query(
        "plantTBL",
        new String[]{"id"},
        "bluetoothAddress = ?",
        new String[]{bluetoothAddress},
        null, null, null
    );
    
    if (cursor.moveToFirst()) {
        plantId = cursor.getInt(0);
    }
    
    cursor.close();
    db.close();
    return plantId;
}
```

### ✏️ 화분 수정 (UPDATE)

```java
public int updatePlant(int plantId, String plantName, String plantType, int wateringInterval) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    
    values.put("plantName", plantName);
    values.put("plantType", plantType);
    values.put("wateringInterval", wateringInterval);
    
    int affectedRows = db.update(
        "plantTBL",
        values,
        "id = ?",
        new String[]{String.valueOf(plantId)}
    );
    
    db.close();
    return affectedRows;  // 업데이트된 행 수
}
```

**사용 예시:**
```java
int updated = updatePlant(1, "베란다 몬스테라", "관엽식물", 5);
if (updated > 0) {
    Toast.makeText(this, "화분 정보 수정 완료", Toast.LENGTH_SHORT).show();
}
```

### 🗑️ 화분 삭제 (DELETE)

#### 소프트 삭제 (권장) - isActive를 0으로 설정
```java
public int deactivatePlant(int plantId) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put("isActive", 0);
    
    int affectedRows = db.update(
        "plantTBL",
        values,
        "id = ?",
        new String[]{String.valueOf(plantId)}
    );
    
    db.close();
    return affectedRows;
}
```

#### 하드 삭제 (실제 삭제) - 센서 데이터도 같이 삭제
```java
public boolean deletePlant(int plantId) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    
    try {
        db.beginTransaction();
        
        // 1. 관련 센서 데이터 먼저 삭제
        db.delete("sensorData", "plantId = ?", new String[]{String.valueOf(plantId)});
        
        // 2. 화분 삭제
        int deleted = db.delete("plantTBL", "id = ?", new String[]{String.valueOf(plantId)});
        
        db.setTransactionSuccessful();
        return deleted > 0;
        
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    } finally {
        db.endTransaction();
        db.close();
    }
}
```

---

## 3. 센서 데이터 관리

### 📊 테이블 구조
```sql
sensorData (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plantId INTEGER NOT NULL,
    timestamp INTEGER NOT NULL,
    temperature REAL,
    humidity REAL,
    soilMoisture REAL,
    lightLevel INTEGER,
    FOREIGN KEY(plantId) REFERENCES plantTBL(id)
)
```

### ➕ 센서 데이터 추가

```java
public long insertSensorData(int plantId, float temperature, float humidity, 
                             float soilMoisture, int lightLevel) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    
    values.put("plantId", plantId);
    values.put("timestamp", System.currentTimeMillis());
    values.put("temperature", temperature);
    values.put("humidity", humidity);
    values.put("soilMoisture", soilMoisture);
    values.put("lightLevel", lightLevel);
    
    long newRowId = db.insert("sensorData", null, values);
    db.close();
    
    return newRowId;
}
```

**사용 예시:**
```java
// 블루투스로 "25.5,60.2,320,512" 수신 시
String data = "25.5,60.2,320,512";
String[] values = data.split(",");

float temp = Float.parseFloat(values[0]);
float humidity = Float.parseFloat(values[1]);
float soilMoisture = Float.parseFloat(values[2]);
int light = Integer.parseInt(values[3]);

long dataId = insertSensorData(1, temp, humidity, soilMoisture, light);
```

### 🔍 센서 데이터 조회

#### 특정 화분의 최근 데이터 조회
```java
public SensorData getLatestSensorData(int plantId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    SensorData data = null;
    
    Cursor cursor = db.query(
        "sensorData",
        null,
        "plantId = ?",
        new String[]{String.valueOf(plantId)},
        null, null,
        "timestamp DESC",  // 최신순
        "1"                // LIMIT 1
    );
    
    if (cursor.moveToFirst()) {
        data = new SensorData();
        data.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        data.plantId = cursor.getInt(cursor.getColumnIndexOrThrow("plantId"));
        data.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
        data.temperature = cursor.getFloat(cursor.getColumnIndexOrThrow("temperature"));
        data.humidity = cursor.getFloat(cursor.getColumnIndexOrThrow("humidity"));
        data.soilMoisture = cursor.getFloat(cursor.getColumnIndexOrThrow("soilMoisture"));
        data.lightLevel = cursor.getInt(cursor.getColumnIndexOrThrow("lightLevel"));
    }
    
    cursor.close();
    db.close();
    return data;
}
```

#### 특정 기간의 데이터 조회 (그래프용)
```java
public List<SensorData> getSensorDataByPeriod(int plantId, long startTime, long endTime) {
    List<SensorData> dataList = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    
    Cursor cursor = db.query(
        "sensorData",
        null,
        "plantId = ? AND timestamp BETWEEN ? AND ?",
        new String[]{
            String.valueOf(plantId),
            String.valueOf(startTime),
            String.valueOf(endTime)
        },
        null, null,
        "timestamp ASC"  // 시간순 정렬
    );
    
    while (cursor.moveToNext()) {
        SensorData data = new SensorData();
        data.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        data.plantId = cursor.getInt(cursor.getColumnIndexOrThrow("plantId"));
        data.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
        data.temperature = cursor.getFloat(cursor.getColumnIndexOrThrow("temperature"));
        data.humidity = cursor.getFloat(cursor.getColumnIndexOrThrow("humidity"));
        data.soilMoisture = cursor.getFloat(cursor.getColumnIndexOrThrow("soilMoisture"));
        data.lightLevel = cursor.getInt(cursor.getColumnIndexOrThrow("lightLevel"));
        
        dataList.add(data);
    }
    
    cursor.close();
    db.close();
    return dataList;
}
```

#### 최근 24시간 데이터 조회
```java
public List<SensorData> getLast24HoursData(int plantId) {
    long now = System.currentTimeMillis();
    long yesterday = now - (24 * 60 * 60 * 1000);  // 24시간 전
    
    return getSensorDataByPeriod(plantId, yesterday, now);
}
```

### 🗑️ 오래된 데이터 삭제 (성능 최적화)

```java
public int deleteOldSensorData(int daysToKeep) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    
    long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L);
    
    int deleted = db.delete(
        "sensorData",
        "timestamp < ?",
        new String[]{String.valueOf(cutoffTime)}
    );
    
    db.close();
    return deleted;  // 삭제된 행 수
}
```

**사용 예시:**
```java
// 30일 이상 된 데이터 삭제
int deleted = deleteOldSensorData(30);
Log.d("DB", "오래된 데이터 " + deleted + "개 삭제됨");
```

---

## 4. 고급 쿼리

### 📈 통계 데이터 조회

#### 특정 화분의 평균값 계산
```java
public PlantStatistics getPlantStatistics(int plantId, long startTime, long endTime) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    PlantStatistics stats = new PlantStatistics();
    
    String query = "SELECT " +
        "AVG(temperature) as avgTemp, " +
        "AVG(humidity) as avgHumidity, " +
        "AVG(soilMoisture) as avgSoil, " +
        "MIN(temperature) as minTemp, " +
        "MAX(temperature) as maxTemp, " +
        "COUNT(*) as dataCount " +
        "FROM sensorData " +
        "WHERE plantId = ? AND timestamp BETWEEN ? AND ?";
    
    Cursor cursor = db.rawQuery(query, new String[]{
        String.valueOf(plantId),
        String.valueOf(startTime),
        String.valueOf(endTime)
    });
    
    if (cursor.moveToFirst()) {
        stats.avgTemperature = cursor.getFloat(0);
        stats.avgHumidity = cursor.getFloat(1);
        stats.avgSoilMoisture = cursor.getFloat(2);
        stats.minTemperature = cursor.getFloat(3);
        stats.maxTemperature = cursor.getFloat(4);
        stats.dataCount = cursor.getInt(5);
    }
    
    cursor.close();
    db.close();
    return stats;
}
```

### 🔗 JOIN 쿼리 - 화분 정보와 최신 센서 데이터

```java
public List<PlantWithLatestData> getAllPlantsWithLatestData() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    List<PlantWithLatestData> result = new ArrayList<>();
    
    String query = 
        "SELECT p.id, p.plantName, p.plantType, p.bluetoothAddress, " +
        "       s.temperature, s.humidity, s.soilMoisture, s.timestamp " +
        "FROM plantTBL p " +
        "LEFT JOIN sensorData s ON p.id = s.plantId " +
        "WHERE p.isActive = 1 " +
        "AND s.id = (SELECT id FROM sensorData WHERE plantId = p.id ORDER BY timestamp DESC LIMIT 1) " +
        "ORDER BY p.id";
    
    Cursor cursor = db.rawQuery(query, null);
    
    while (cursor.moveToNext()) {
        PlantWithLatestData item = new PlantWithLatestData();
        item.plantId = cursor.getInt(0);
        item.plantName = cursor.getString(1);
        item.plantType = cursor.getString(2);
        item.bluetoothAddress = cursor.getString(3);
        item.latestTemperature = cursor.getFloat(4);
        item.latestHumidity = cursor.getFloat(5);
        item.latestSoilMoisture = cursor.getFloat(6);
        item.lastUpdateTime = cursor.getLong(7);
        
        result.add(item);
    }
    
    cursor.close();
    db.close();
    return result;
}
```

---

## 5. 실전 예제

### 예제 1: MainActivity에서 화분 목록 표시

```java
public class MainActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private ListView plantListView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // DBHelper 생성
        dbHelper = new DBHelper(this);
        
        // 화분 목록 불러오기
        loadPlantList();
    }
    
    private void loadPlantList() {
        List<Plant> plants = getAllPlants();
        
        if (plants.isEmpty()) {
            Toast.makeText(this, "등록된 화분이 없습니다", Toast.LENGTH_SHORT).show();
        } else {
            // ListView 또는 RecyclerView에 표시
            // PlantAdapter adapter = new PlantAdapter(this, plants);
            // plantListView.setAdapter(adapter);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 중요: Activity 종료 시 DBHelper 닫기
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
```

### 예제 2: 블루투스 Service에서 데이터 저장

```java
public class BluetoothService extends Service {
    private DBHelper dbHelper;
    
    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(this);
    }
    
    private void onBluetoothDataReceived(String bluetoothAddress, String data) {
        // 1. 블루투스 주소로 화분 찾기
        int plantId = getPlantIdByBluetoothAddress(bluetoothAddress);
        
        if (plantId == -1) {
            Log.e("BluetoothService", "등록되지 않은 기기: " + bluetoothAddress);
            return;
        }
        
        // 2. 데이터 파싱
        try {
            String[] values = data.split(",");
            float temperature = Float.parseFloat(values[0]);
            float humidity = Float.parseFloat(values[1]);
            float soilMoisture = Float.parseFloat(values[2]);
            int lightLevel = Integer.parseInt(values[3]);
            
            // 3. 데이터베이스에 저장
            long dataId = insertSensorData(plantId, temperature, humidity, 
                                          soilMoisture, lightLevel);
            
            if (dataId != -1) {
                Log.d("BluetoothService", "센서 데이터 저장 완료: " + dataId);
                
                // 4. 알림 조건 체크
                checkAlertConditions(plantId, soilMoisture, temperature);
            }
            
        } catch (Exception e) {
            Log.e("BluetoothService", "데이터 파싱 오류: " + e.getMessage());
        }
    }
    
    private void checkAlertConditions(int plantId, float soilMoisture, float temperature) {
        Plant plant = getPlantById(plantId);
        
        if (soilMoisture < 300) {
            sendNotification(plant.plantName + " 물 부족", 
                           "토양 습도: " + soilMoisture);
        }
        
        if (temperature > 35 || temperature < 5) {
            sendNotification(plant.plantName + " 온도 경고", 
                           "현재 온도: " + temperature + "°C");
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
```

### 예제 3: 새 화분 등록

```java
public class AddPlantActivity extends AppCompatActivity {
    private EditText etPlantName, etPlantType;
    private TextView tvBluetoothAddress;
    private Button btnSave;
    private DBHelper dbHelper;
    private String selectedBluetoothAddress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);
        
        dbHelper = new DBHelper(this);
        
        etPlantName = findViewById(R.id.et_plant_name);
        etPlantType = findViewById(R.id.et_plant_type);
        tvBluetoothAddress = findViewById(R.id.tv_bluetooth_address);
        btnSave = findViewById(R.id.btn_save);
        
        btnSave.setOnClickListener(v -> savePlant());
    }
    
    private void savePlant() {
        String plantName = etPlantName.getText().toString().trim();
        String plantType = etPlantType.getText().toString().trim();
        
        if (plantName.isEmpty() || plantType.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedBluetoothAddress == null) {
            Toast.makeText(this, "블루투스 기기를 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        
        long plantId = insertPlant(plantName, plantType, 
                                   selectedBluetoothAddress, "HC-06");
        
        if (plantId != -1) {
            Toast.makeText(this, "화분 등록 완료!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "등록 실패. 이미 등록된 기기일 수 있습니다.", 
                          Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
```

### 예제 4: 그래프용 데이터 조회

```java
public class ChartActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private int plantId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        
        dbHelper = new DBHelper(this);
        plantId = getIntent().getIntExtra("plantId", -1);
        
        loadChartData();
    }
    
    private void loadChartData() {
        // 최근 7일 데이터 조회
        long now = System.currentTimeMillis();
        long weekAgo = now - (7 * 24 * 60 * 60 * 1000L);
        
        List<SensorData> dataList = getSensorDataByPeriod(plantId, weekAgo, now);
        
        // 차트 라이브러리에 데이터 전달
        List<Entry> temperatureEntries = new ArrayList<>();
        List<Entry> soilMoistureEntries = new ArrayList<>();
        
        for (int i = 0; i < dataList.size(); i++) {
            SensorData data = dataList.get(i);
            temperatureEntries.add(new Entry(i, data.temperature));
            soilMoistureEntries.add(new Entry(i, data.soilMoisture));
        }
        
        // MPAndroidChart 등의 라이브러리로 그래프 표시
        // ...
    }
}
```

---

## 📌 데이터 모델 클래스 (참고)

### Plant.java
```java
public class Plant {
    public int id;
    public String plantName;
    public String plantType;
    public String bluetoothAddress;
    public String bluetoothName;
    public long registeredDate;
    public int wateringInterval;
    public int isActive;
}
```

### SensorData.java
```java
public class SensorData {
    public int id;
    public int plantId;
    public long timestamp;
    public float temperature;
    public float humidity;
    public float soilMoisture;
    public int lightLevel;
}
```

### PlantStatistics.java
```java
public class PlantStatistics {
    public float avgTemperature;
    public float avgHumidity;
    public float avgSoilMoisture;
    public float minTemperature;
    public float maxTemperature;
    public int dataCount;
}
```

---

## ⚠️ 주의사항

1. **메모리 누수 방지**
   - `dbHelper.close()` 항상 호출
   - `Cursor.close()` 항상 호출
   - try-finally 또는 try-with-resources 사용 권장

2. **트랜잭션 사용**
   - 여러 작업을 한 번에 수행할 때 트랜잭션 사용
   - 성능 향상 및 데이터 일관성 보장

3. **UI 스레드 주의**
   - 대량 데이터 조회는 백그라운드 스레드에서 실행
   - AsyncTask, Thread, 또는 Coroutine 사용

4. **데이터 백업**
   - 중요한 데이터는 주기적으로 백업
   - 외부 저장소 또는 클라우드 동기화 권장

---

## 🎯 성능 최적화 팁

1. **인덱스 생성** (자주 검색하는 컬럼)
```java
db.execSQL("CREATE INDEX idx_bluetooth_address ON plantTBL(bluetoothAddress)");
db.execSQL("CREATE INDEX idx_plant_timestamp ON sensorData(plantId, timestamp)");
```

2. **배치 삽입** (여러 데이터를 한 번에)
```java
db.beginTransaction();
try {
    for (SensorData data : dataList) {
        db.insert("sensorData", null, values);
    }
    db.setTransactionSuccessful();
} finally {
    db.endTransaction();
}
```

3. **오래된 데이터 정기 삭제**
```java
// 매일 자정에 30일 이상 된 데이터 삭제
deleteOldSensorData(30);
```

---

## 📞 문제 해결

### "table already exists" 오류
```java
// 해결: 앱 삭제 후 재설치 또는 버전 번호 증가
```

### 데이터가 저장되지 않음
```java
// 확인사항:
// 1. getWritableDatabase() 사용 확인
// 2. insert() 반환값 확인 (-1이면 실패)
// 3. 제약조건 위반 (UNIQUE, NOT NULL) 확인
```

### 앱이 느림
```java
// 해결:
// 1. 인덱스 추가
// 2. 백그라운드 스레드에서 DB 작업
// 3. 오래된 데이터 삭제
// 4. LIMIT 사용하여 조회 데이터 제한
```

---

**작성일:** 2025-10-14  
**프로젝트:** SmartPot  
**버전:** 1.0

