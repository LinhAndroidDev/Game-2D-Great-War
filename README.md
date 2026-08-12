## Intro Game Great War
![Image](https://github.com/user-attachments/assets/01447a67-014f-4dc9-b7ae-e80b71d8f0c4)

# Game Design — Progression & Enemies

Tài liệu mô tả logic cấp độ, quái, boss và quy tắc combat (đã chốt). Dùng làm nguồn tham chiếu khi implement / review.

---

## 1. Mục tiêu

Chuyển game từ “quái random 1 hit” sang **4 cấp độ** với:

- Quái có **thanh máu**
- Mỗi loại quái có **kỹ năng riêng**
- **Boss gate** theo số kill trong level
- Kết thúc bằng dialog **chúc mừng** khi hạ boss cuối

---

## 2. Quy tắc chung

### 2.1 Thanh máu

| Đối tượng | Máu |
|-----------|-----|
| Player | 5 |
| Quái `ic_ghost_white` | 1 |
| Quái `ic_ghost_scary` | 2 |
| Quái `ic_ghost_blue` | 3 |
| Quái `ic_bat` | 3 |
| Boss level 1 (`white`) | 10 |
| Boss level 2 (`scary`) | 20 |
| Boss level 3 (`blue`) | 30 |
| Boss level 4 (`bat`) | 40 |

- Mỗi viên đạn player trúng = **-1 HP** (trừ khi mục tiêu đang bất tử / ẩn).
- Thanh máu quái hiển thị trên đầu quái (tương tự player).

### 2.2 Sát thương lên player

| Nguồn | Damage |
|-------|--------|
| Chạm quái (đang hiện / có hitbox) | 1 |
| Đạn quái | 1 |
| Chạm Lava (map hoặc do quái tạo) | 2 / giây (cooldown 1s) |

Sau khi player bị trừ máu, có **i-frame ~0.5–1s** để tránh mất máu liên tục.

### 2.3 Progress theo level

- Đếm kill **trong level hiện tại** (reset khi sang level mới).
- UI: `Level X` + thanh tiến trình `kills / threshold`.
- Chỉ **quái thường** tính vào progress; kill boss dùng để **clear level**.

### 2.4 Khi boss xuất hiện

1. **Clear toàn bộ quái con** đang có.
2. Clear đạn quái / hazard do enemy tạo (nếu đang tồn tại).
3. Spawn boss gần **tâm viewport** (giữa màn hình camera hiện tại), ô hợp lệ.
4. **Boss di chuyển theo player** (chase).
5. Đặt player cách boss một khoảng cố định (ví dụ phía dưới/trái), ô **walkable**, không chồng hitbox boss.
6. **Tạm dừng spawn quái con** trong suốt boss fight.
7. Hồi **full HP** player khi bắt đầu boss fight.

### 2.5 Khi hạ gục boss

1. Xóa toàn bộ **đạn quái** còn trên map.
2. Xóa toàn bộ **hazard do enemy tạo** (water/lava tạm).
3. Hồi **full HP** player.
4. Sang **level tiếp theo** (hoặc victory nếu là boss cuối).
5. Reset kill counter level mới về 0.
6. Bật lại spawn quái con theo pool level mới.

### 2.6 Hazard do enemy tạo (water / lava nhỏ)

- Kích thước: **1–2 tile**.
- **TTL:** hết hạn sau **8–12 giây** hoặc giới hạn ≤ **8** hazard trên map.
- Không spawn đè lên player.
- Khi boss chết / sang level: **xóa hết** hazard loại này.

### 2.7 Đạn quái (chung)

- Object riêng (ví dụ `EnemyProjectile`).
- Trúng player → -1 HP + i-frame.
- Khi boss chết → **xóa hết đạn quái**.
- Tốc độ đạn vừa phải (chậm hơn đạn player một chút) để có thể né.

---

## 3. Cấp độ chi tiết

### Level 1 — `ic_ghost_white`

**Quái thường**

| Thuộc tính | Giá trị |
|------------|---------|
| Sprite | `ic_ghost_white` |
| HP | 1 |
| Tốc độ | Chậm hơn base một chút |
| Kỹ năng | Mỗi **3 giây** bắn **4 hướng** (0°, 90°, 180°, 270°) |

**Boss (sau 10 kill trong level)**

| Thuộc tính | Giá trị |
|------------|---------|
| Sprite | `ic_ghost_white` scale **×5** |
| HP | 10 |
| Di chuyển | Chase player |
| Kỹ năng | Mỗi **3 giây** bắn **10 hướng** đều quanh vòng tròn |

**Sang Level 2:** khi hạ boss white.

---

### Level 2 — `ic_ghost_scary`

**Quái thường**

| Thuộc tính | Giá trị |
|------------|---------|
| Sprite | `ic_ghost_scary` |
| HP | 2 |
| Chu kỳ ẩn/hiện | **1s ẩn / 1s hiện** (lặp) |
| Khi ẩn | Không nhận đạn player; **không** gây damage khi chạm |
| Khi hiện | Nhận đạn; chạm gây 1 damage |

**Boss (sau 20 kill trong level)**

| Thuộc tính | Giá trị |
|------------|---------|
| Sprite | `ic_ghost_scary` ×5 |
| HP | 20 |
| Di chuyển | Chase player |
| Kỹ năng | Mỗi **3 giây** bắn **15 hướng** |

**Sang Level 3:** khi hạ boss scary.

---

### Level 3 — `ic_ghost_blue`

**Quái thường**

| Thuộc tính | Giá trị |
|------------|---------|
| Sprite | `ic_ghost_blue` |
| HP | 3 |
| Kỹ năng | Mỗi **3 giây** tạo **1 water nhỏ** (1–2 tile) cản đường |

**Boss (sau 10 kill trong level)**

| Thuộc tính | Giá trị |
|------------|---------|
| Sprite | `ic_ghost_blue` ×5 |
| HP | 30 |
| Di chuyển | Chase player |
| Kỹ năng bắn | Mỗi **3 giây** bắn **6 hướng** |
| Đạn đặc biệt | Chạm **cây / nước / lava / biên map** → **nảy (đổi hướng)**; không biến mất vì va chạm |
| Kết thúc đạn | Clear khi **boss chết**; tối đa **18–24** viên tồn tại trên map |

**Sang Level 4:** khi hạ boss blue.

---

### Level 4 — `ic_bat`

**Quái thường**

| Thuộc tính | Giá trị |
|------------|---------|
| Sprite | `ic_bat` |
| HP | 3 |
| Tốc độ | Nhanh hơn base một chút |
| Kỹ năng | Mỗi **3 giây** tạo **1 lava nhỏ** (1–2 tile): cản đường + damage lava |

**Boss (sau 20 kill trong level)**

| Thuộc tính | Giá trị |
|------------|---------|
| Sprite | `ic_bat` ×5 |
| HP | 40 |
| Di chuyển | Chase player |
| Kỹ năng 1 | Mỗi **3 giây** bắn **10 hướng** |
| Kỹ năng 2 | Mỗi **3 giây**: **phản** viên đạn player **đầu tiên** chạm boss → thành đạn boss |

**Kết thúc game:** hạ boss bat → **dialog chúc mừng**. Nút xác nhận → chơi lại từ Level 1.

---

## 4. Bảng tóm tắt progression

| Level | Quái | Threshold kill | Boss HP | Boss shoot |
|-------|------|----------------|---------|------------|
| 1 | White (chậm, 4-way / 3s, HP 1) | 10 | 10 | 10-way / 3s |
| 2 | Scary (ẩn 1s / hiện 1s, HP 2) | 20 | 20 | 15-way / 3s |
| 3 | Blue (tạo water / 3s, HP 3) | 10 | 30 | 6-way bounce / 3s |
| 4 | Bat (nhanh, tạo lava / 3s, HP 3) | 20 | 40 | 10-way + reflect / 3s |

---

## 5. UI cần có

1. **Thanh máu quái / boss** trên đầu entity.
2. **Progress level:** text + bar `kills/threshold` (đổi thành “BOSS” khi đang boss fight).
3. **Dialog Game Over** (giữ).
4. **Dialog Victory / Chúc mừng** khi thắng boss cuối.

---

## 6. Ghi chú kỹ thuật

- Tách **EnemyType** / **EnemyRole** (`NORMAL` | `BOSS`) thay vì random 4 sprite.
- Hitbox scale theo size (boss ×5 → radius ×5).
- Spawn quái theo `currentLevel`; không spawn khi đang boss fight.
- Hazard tạm có tag `temporary` để clear theo batch.
- “Giữa màn hình” = tâm viewport (`GameDisplay`), không phải giữa map.
- Map tile gốc (grass/water/lava random) **không** xoá khi sang level; chỉ xoá hazard **do enemy tạo**.

---

## 7. Quyết định đã chốt

- [x] White thường: bắn **4 hướng / 3 giây**
- [x] Kill counter **theo level**
- [x] Boss xuất hiện: **clear quái con**, boss **chase player**
- [x] Hạ boss / sang level: **full HP**, **xoá hazard enemy**, xoá đạn quái
- [x] Scary: **1s ẩn / 1s hiện**; ẩn = không đạn + không contact damage
- [x] Water/lava tạo ra: nhỏ, có TTL/max, không đè player
- [x] Đạn boss blue: nảy; có **cap số lượng**; clear khi boss chết
- [x] Placement boss theo **tâm viewport** + player offset walkable
