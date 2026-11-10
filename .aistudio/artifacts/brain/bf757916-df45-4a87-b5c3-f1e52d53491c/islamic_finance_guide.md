# Islamic Personal Finance — Feature & Calculation Documentation

## 1. Overview
The **Islamic Personal Finance** suite is tailored for consumer financing, installment calculations, and debt burden evaluations according to Shariah-compliant financing principles:
- **Mode of Financing**: **Tawarruq** (Islamic Personal Financing through commodity Murabaha/Tawarruq)
- **Regulatory Framework**: State Bank of Pakistan (SBP) Consumer Financing Regulations.
- **Application Structure**: Streamlined 4-tab architecture matching Islamic banking theme:
  1. **Income Assessment** (Multiplier-based financing capacity)
  2. **Installment Calculator** (KIBOR + Spread, tenure, PMT calculations)
  3. **DBR Calculator** (Debt Burden Ratio with regular customer and bank staff ceilings)
  4. **Summary Tab**:
     - **Pre-Entry Modal Dialog**: Clicking "Summary" opens a focused popup prompting for **Customer Full Name** and **CNIC Number** (with auto-hyphen 13-digit format `XXXXX-XXXXXXX-X`) plus customer classification.
     - **Profile Header**: Shows assessed customer credentials with an *✏️ Edit Details* button to quickly re-open the dialog.
     - **Consolidated Assessment**: Aggregates Income multiple, Financing terms (PMT, KIBOR + spread, profit, deferred price), and DBR metrics with Shariah Tawarruq notice.
     - **Mathematical & Tabular Alignment**: Numbers, percentages, and financial values are rendered with strict right alignment in a fixed two-column layout (`115mm` parameter column, `67mm` right-aligned value column), standard `PKR ` currency formatting without Unicode clipping, and synchronized header-to-body alignments across both web UI and exported A4 PDF sheets.
     - **Native Mobile A4 Share**: Save and Print buttons are removed from the bottom in favor of a single high-priority **↗ Share Assessment Sheet (A4 PDF)** button. Clicking triggers the mobile app's native Share Sheet (`window.Android.sharePdf` or Web Share API with File) to send the A4 assessment sheet as a document via WhatsApp, Gmail, etc.

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

## 4. Navigation & Layout Optimization

### 4.1. Clean 3-Tab Architecture
- The top navigation bar now hosts exactly three focused modules:
  - **Income Assessment**: Computes max financing entitlement based on net monthly salary and industry multiplier rules.
  - **Installment**: Determines exact monthly payment (PMT), total profit, and total payable amount with a quick transition to DBR analysis.
  - **DBR Calculator**: Evaluates total monthly debt burden against regulatory ceilings with multi-commitment tracking.
- The previous *Amortization* and *Documents Checklist* tabs have been completely removed to provide a faster, zero-clutter experience.

### 4.2. Refined Watermark Branding Badge
- At the base of the application, a balanced and elegant watermark badge is implemented:
  - Text: **Powered by Syed Muhammad Humza Arif** with Islamic crescent emblem (`☪`).
  - Styling: Soft, polished corporate slate-navy (`#2c4b6c`) on a crisp white pill badge (`#ffffff`), with a delicate border (`#d4e2ee`) and subtle elevation. It is clearly legible and visible while maintaining a clean, non-intrusive aesthetic.

---

## 5. Mobile Android APK Native Bridge & Performance

### 5.1. Mobile Smoothness & Lag-Free Optimizations
- **Touch Latency**: Enforced `touch-action: manipulation;` across all buttons, tabs, and form controls to remove 300ms tap delay.
- **GPU Backdrop Elimination**: Replaced high-cost CSS `backdrop-filter: blur(...)` with GPU-accelerated translucent RGBA fills, maintaining 60fps modal transitions.
- **Input Responsiveness**: Input formatting preserves selection range and cursor coordinates to prevent virtual keyboard jitter and dropped keystrokes on mobile devices.
- **Asset Mirroring**: Full synchronization between `/index.html` and `/www/index.html` ensuring web and mobile Capacitor APK builds stay identical.
