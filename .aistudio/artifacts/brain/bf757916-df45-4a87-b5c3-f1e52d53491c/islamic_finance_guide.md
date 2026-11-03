# Islamic Personal Finance — Feature & Calculation Documentation

## 1. Overview
The **Islamic Personal Finance** suite is tailored for consumer financing, installment calculations, debt burden evaluations, and tentative repayment schedule generation according to Shariah-compliant financing principles:
- **Mode of Financing**: **Tawarruq** (Islamic Personal Financing through commodity Murabaha/Tawarruq)
- **Schedule Classification**: **Tentative Repayment Schedule**
- **Regulatory Framework**: State Bank of Pakistan (SBP) Consumer Financing Regulations.

---

## 2. Mode of Financing: Tawarruq
Under the **Tawarruq** structure:
1. The financial institution purchases commodities from an approved broker or international commodity market on a spot cash basis.
2. The institution sells the specified commodity to the customer at a deferred selling price (cost price plus agreed profit rate: KIBOR + Bank Spread) on an installment basis.
3. The customer (or designated agent) sells the commodity to a third-party buyer on a spot cash basis to obtain immediate liquid funds.
4. Repayment is structured across regular monthly installments covering principal liquidation and pre-determined profit portions.

---

## 3. Debt Burden Ratio (DBR) Thresholds

The application supports segmented DBR evaluation based on customer classification:

| Customer Segment | Regulatory DBR Limit | Benchmark & Scope |
| :--- | :--- | :--- |
| **Regular Customer** | **35.00%** | SBP Consumer Financing Regulation standard ceiling for salaried and self-employed applicants. |
| **Bank Staff** | **40.00%** | Concessionary staff threshold allowing higher allowable debt burden for bank employees. |

### Mathematical Formulation
$$\text{DBR (\%)} = \left( \frac{\text{Main Finance Installment} + \sum \text{Other EMIs}}{\text{Net Monthly Income}} \right) \times 100$$

$$\text{Max Permissible Monthly Obligation} = \text{Net Monthly Income} \times \text{Threshold}$$

### Status Indicator
- **Within Limit (Green)**: When $\text{DBR} \le 35\%$ (or $\le 40\%$ for staff). Displays available financial headroom in PKR.
- **Exceeds Threshold (Red/Amber)**: When $\text{DBR} > 35\%$ (or $> 40\%$ for staff). Displays excess over-limit obligation in PKR.

---

## 4. Tentative Repayment Schedule & Theme Formatting

### 4.1. Official English Notice Wording
> **Important Notice: Tentative Schedule**
> *This repayment schedule is purely tentative and indicative. The final repayment schedule will be determined and issued upon financing disbursement based on prevailing KIBOR, disbursement date, and bank's final approval.*

This notice is displayed in the main on-screen report card and in-app preview modal, while the generated PDF export maintains a clean, presentation-ready layout without bottom notice clutter.

### 4.2. Streamlined User Flow
- **Main Amortization Tab**: Streamlined to a single, focused action button: **👁️ Preview Amortization Report**. Outer quick-print and share buttons have been removed to eliminate clutter.
- **Inside Preview Modal (Popup)**:
  - Formatted in the exact application card theme with clean typography and badges.
  - Dedicated **↗ Share PDF** button (triggers native device share sheet/WhatsApp, Capacitor plugin, or Web Share).
  - Relationship Manager endorsement and customer acknowledgment signature blocks.
  - Streamlined header with Print and Save buttons removed for a focused, clutter-free mobile experience.

---

## 5. Mobile Android APK Native Bridge & Performance

### 5.1. Native Android Bridge (`window.Android`)
The Android APK environment routes media and printing tasks through the native Java interface:
- **`savePdf(base64, fileName, mimeType)`**: Saves PDFs directly into the public `Downloads` directory using Android `MediaStore` (API 29+) with zero dangerous permission prompts, accompanied by a native Android Toast notification.
- **`sharePdf(base64, fileName, mimeType)`**: Writes the decoded PDF into secure app cache and launches Android's native Share Sheet via `FileProvider` (`content://com.humzaarif.islamicfinance.fileprovider/...`).
- **`printHtml(html, title)`**: Dynamically loads formatted printable schedule HTML into a background WebView and invokes Android's `PrintManager` with A4 landscape print adapters.

### 5.2. Mobile Smoothness & Lag-Free Optimizations
- **Touch Latency**: Enforced `touch-action: manipulation;` across all buttons, tabs, and form controls to remove 300ms tap delay.
- **GPU Backdrop Elimination**: Replaced high-cost CSS `backdrop-filter: blur(...)` with GPU-accelerated translucent RGBA fills, maintaining 60fps modal transitions.
- **Input Responsiveness**: Input formatting preserves selection range and cursor coordinates to prevent virtual keyboard jitter and dropped keystrokes on mobile devices.
