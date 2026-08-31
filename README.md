# AutoFarmPro

Plugin Minecraft (Paper/Spigot) tự động làm vườn (trồng + thu hoạch) và tự động câu cá, kích hoạt theo **từng người chơi** khi họ đứng trong vùng quét quanh vị trí của mình.

## Tính năng chính

- **Tự động thu hoạch**: cây trồng trong vùng quét chín tới đâu, thu hoạch tới đó, vật phẩm vào thẳng túi đồ.
- **Tự động hồi sinh**: cây được reset về giai đoạn non ngay lập tức sau khi thu hoạch (không cần trồng lại tay).
- **Tự động trồng mới**: nếu ô đất trống (farmland/soul sand) và bạn cầm/có hạt giống phù hợp trong túi đồ, plugin tự trồng luôn (trừ 1 hạt giống, trừ khi bạn ở Creative).
- **Vùng quét tùy chỉnh**: mặc định `radius: 1` + `range-y: 1` = đúng **vùng 3x3** quanh người chơi như ví dụ bạn đưa ra, chỉnh thoải mái trong `config.yml`.
- **Hỗ trợ nhiều loại cây**: Lúa mì, Cà rốt, Khoai tây, Củ dền, **Bứu Địa Ngục (Nether Wart)**, Cacao, Mía, Xương rồng, Tre, Dưa hấu, Bí ngô.
- **Tự động câu cá**: tự "giật cần" khi cá cắn câu (có độ trễ ngẫu nhiên cho tự nhiên), tự thả cần lại, tự trừ độ bền cần câu giống vanilla.
- **Bật/tắt riêng theo người chơi**: `/autofarm farm`, `/autofarm fish` — mỗi người tự bật cho mình, không ép cả server.
- **Thống kê**: `/autofarm stats` xem số cây đã thu hoạch / cá đã câu.
- **An toàn hiệu năng**: giới hạn số block xử lý mỗi lượt quét (`max-blocks-per-scan`), giới hạn theo thế giới (`enabled-worlds`).
- **Hiệu ứng phản hồi**: hạt hiệu ứng + âm thanh khi thu hoạch (tắt được).

## Yêu cầu

- Java 17+
- Server Paper hoặc Spigot 1.20.x trở lên

## Cách lấy file .jar

Môi trường tạo ra project này không có kết nối mạng nên không build sẵn được file `.jar` (Maven cần tải thư viện Paper API từ internet). Chọn 1 trong 2 cách dưới đây — cách 1 **không cần cài gì** trên máy bạn.

### Cách 1 — Build tự động trên GitHub (khuyên dùng, không cần cài Java/Maven)

Project đã có sẵn file `.github/workflows/build.yml` để GitHub tự build giúp bạn:

1. Tạo tài khoản GitHub (miễn phí) nếu chưa có, tạo 1 repository mới (để Public hoặc Private đều được).
2. Đưa **toàn bộ** thư mục `AutoFarmPro` (đã giải nén) lên repo đó — dễ nhất là dùng app **GitHub Desktop** (kéo-thả, không cần gõ lệnh git), hoặc dùng nút "Add file → Upload files" trên trang web GitHub.
3. Vào tab **Actions** trong repo → sẽ thấy workflow "Build AutoFarmPro" tự chạy (khoảng 1–2 phút). Nếu không tự chạy, bấm workflow đó → **Run workflow**.
4. Sau khi chạy xong (dấu tích xanh ✅), bấm vào lần chạy đó → kéo xuống mục **Artifacts** → tải file `AutoFarmPro-jar.zip` → giải nén ra sẽ có `AutoFarmPro.jar`.
5. Copy file `.jar` này vào thư mục `plugins/` của server rồi khởi động lại.

### Cách 2 — Build trên máy đang chạy server (nếu có terminal/SSH)

Máy chạy được server Paper/Spigot thì chắc chắn đã có sẵn Java rồi, chỉ cần cài thêm Maven:

```bash
# Ubuntu/Debian
sudo apt install maven -y

# Windows (dùng Chocolatey) hoặc tải tay tại maven.apache.org
choco install maven

# macOS
brew install maven
```

Sau đó vào thư mục `AutoFarmPro` (chỗ chứa file `pom.xml`) và chạy:

```bash
mvn clean package
```

File `.jar` nằm ở `target/AutoFarmPro.jar`. Copy vào thư mục `plugins/` của server rồi khởi động lại.

## Lệnh

| Lệnh | Mô tả | Quyền |
|---|---|---|
| `/autofarm farm` | Bật/tắt tự động làm vườn cho bản thân | `autofarm.use` |
| `/autofarm fish` | Bật/tắt tự động câu cá cho bản thân | `autofarm.use` |
| `/autofarm stats` | Xem thống kê cá nhân | `autofarm.use` |
| `/autofarm reload` | Tải lại config.yml | `autofarm.admin` |

Alias: `/af`, `/farm`. Mặc định `autofarm.use` cho tất cả người chơi (`default: true`) — nếu muốn giới hạn theo rank (VIP...), chỉnh lại node này bằng plugin permission (LuckPerms...).

## Cấu hình nhanh (`config.yml`)

- `farm.radius` + `farm.range-y`: kích thước vùng quét. `radius: 1, range-y: 1` → đúng vùng 3x3x3 như ví dụ Bứu Địa Ngục của bạn.
- `farm.crops`: bật/tắt riêng từng loại cây.
- `farm.auto-plant`: tắt nếu chỉ muốn tự thu hoạch, không muốn tự trồng mới (tiết kiệm hạt giống).
- `fishing.*`: chỉnh độ trễ giật cần / thả cần lại, có tắt auto-recast được.
- `general.enabled-worlds`: để trống = mọi thế giới, hoặc liệt kê tên thế giới cụ thể.

## Ghi chú kỹ thuật & hướng mở rộng

- Trạng thái bật/tắt farm/fish của người chơi lưu trong RAM, **reset khi restart server**. Muốn lưu vĩnh viễn thì đây là điểm dễ chỉnh nhất — thêm đọc/ghi file YAML hoặc database vào `PlayerStateManager`.
- Cocoa, Mía, Xương rồng, Tre, Dưa hấu, Bí ngô: chỉ hỗ trợ tự **thu hoạch + hồi sinh**, KHÔNG tự trồng từ đầu (cần điều kiện đặt phức tạp hơn: khúc gỗ, cạnh nước, cát...).
- Muốn giới hạn chỉ hoạt động trong đất riêng của người chơi (chống lạm dụng farm người khác)? Tích hợp WorldGuard/claim-plugin ngay trong `AutoFarmTask` — mình đã để sẵn comment `TODO` đúng chỗ cần chèn điều kiện kiểm tra.
- Nếu nâng cấp `pom.xml`/`plugin.yml` lên Paper API 1.21+, một số tên `Particle` có thể đổi (ví dụ `VILLAGER_HAPPY` → `HAPPY_VILLAGER`) — để ý nếu build lỗi ở `CropUtils.java`.
- Muốn thêm auto-trồng lại cây rừng (chặt gỗ tự trồng sapling)? Phức tạp hơn nhiều (cần nhận diện cả cây, đúng loại gỗ) — gợi ý làm ở bản sau nếu cần.

Cần chỉnh sửa gì thêm (thêm loại cây, đổi cơ chế, thêm GUI cấu hình, lưu trạng thái vĩnh viễn...) cứ nói mình nhé.
