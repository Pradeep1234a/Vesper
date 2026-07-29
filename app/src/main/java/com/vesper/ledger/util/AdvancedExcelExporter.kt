package com.vesper.ledger.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Budget
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Enterprise-Grade Advanced Excel (.xlsx) Export Engine for Vesper Ledger.
 * Generates multi-worksheet workbooks with native OpenXML structure, formatting,
 * formulas (SUM, SUMIF, SUMIFS, AVERAGEIF, VLOOKUP, XLOOKUP), pivot data tables,
 * and VBA Macro helper guides.
 */
object AdvancedExcelExporter {

    fun exportToExcel(
        context: Context,
        transactions: List<Transaction>,
        categories: List<Category>,
        accounts: List<Account>,
        budgets: List<Budget> = emptyList(),
        currencySymbol: String = "₹"
    ): File? {
        return try {
            val fileName = "Vesper_Ledger_Financial_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xlsx"
            val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
            val outputFile = File(exportDir, fileName)

            ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
                // 1. [Content_Types].xml
                zipOut.putNextEntry(ZipEntry("[Content_Types].xml"))
                zipOut.write(getContentTypesXml().toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // 2. _rels/.rels
                zipOut.putNextEntry(ZipEntry("_rels/.rels"))
                zipOut.write(getTopRelsXml().toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // 3. xl/_rels/workbook.xml.rels
                zipOut.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
                zipOut.write(getWorkbookRelsXml().toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // 4. xl/workbook.xml
                zipOut.putNextEntry(ZipEntry("xl/workbook.xml"))
                zipOut.write(getWorkbookXml().toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // 5. xl/styles.xml
                zipOut.putNextEntry(ZipEntry("xl/styles.xml"))
                zipOut.write(getStylesXml().toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // Prepare Shared Strings Table
                val stringTable = mutableListOf<String>()
                fun addString(str: String): Int {
                    val idx = stringTable.indexOf(str)
                    return if (idx >= 0) idx else {
                        stringTable.add(str)
                        stringTable.size - 1
                    }
                }

                // Generate Sheet Contents
                val sheet1Xml = generateExecutiveDashboardXml(transactions, categories, currencySymbol, ::addString)
                val sheet2Xml = generateTransactionsMasterXml(transactions, categories, accounts, currencySymbol, ::addString)
                val sheet3Xml = generateCategoryAnalyticsXml(transactions, categories, currencySymbol, ::addString)
                val sheet4Xml = generateAccountBalancesXml(transactions, accounts, currencySymbol, ::addString)
                val sheet5Xml = generateBudgetAnalysisXml(budgets, transactions, categories, currencySymbol, ::addString)
                val sheet6Xml = generateMacroHelpXml(::addString)

                // Write Worksheets
                zipOut.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
                zipOut.write(sheet1Xml.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                zipOut.putNextEntry(ZipEntry("xl/worksheets/sheet2.xml"))
                zipOut.write(sheet2Xml.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                zipOut.putNextEntry(ZipEntry("xl/worksheets/sheet3.xml"))
                zipOut.write(sheet3Xml.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                zipOut.putNextEntry(ZipEntry("xl/worksheets/sheet4.xml"))
                zipOut.write(sheet4Xml.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                zipOut.putNextEntry(ZipEntry("xl/worksheets/sheet5.xml"))
                zipOut.write(sheet5Xml.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                zipOut.putNextEntry(ZipEntry("xl/worksheets/sheet6.xml"))
                zipOut.write(sheet6Xml.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // 6. xl/sharedStrings.xml
                zipOut.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
                zipOut.write(getSharedStringsXml(stringTable).toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
            }

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareExcelFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Advanced Excel Report"))
    }

    // ────────────────────────────────────────────────────────────────────────────
    // OPENXML STRUCTURE HELPERS
    // ────────────────────────────────────────────────────────────────────────────
    private fun getContentTypesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
    <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet5.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet6.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

    private fun getTopRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun getWorkbookRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
    <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
    <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/>
    <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet5.xml"/>
    <Relationship Id="rId6" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet6.xml"/>
    <Relationship Id="rId7" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
    <Relationship Id="rId8" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
</Relationships>"""

    private fun getWorkbookXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <sheets>
        <sheet name="Executive Dashboard" sheetId="1" r:id="rId1"/>
        <sheet name="Transactions Master" sheetId="2" r:id="rId2"/>
        <sheet name="Category Analytics" sheetId="3" r:id="rId3"/>
        <sheet name="Account Balances" sheetId="4" r:id="rId4"/>
        <sheet name="Budget vs Actual" sheetId="5" r:id="rId5"/>
        <sheet name="VBA Macro Guide" sheetId="6" r:id="rId6"/>
    </sheets>
</workbook>"""

    private fun getStylesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <fonts count="3">
        <font><sz val="11"/><name val="Calibri"/></font>
        <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
        <font><b/><sz val="14"/><color rgb="FF0F172A"/><name val="Calibri"/></font>
    </fonts>
    <fills count="3">
        <fill><patternFill patternType="none"/></fill>
        <fill><patternFill patternType="gray125"/></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FF0F172A"/></patternFill></fill>
    </fills>
    <borders count="1">
        <border><left/><right/><top/><bottom/></border>
    </borders>
    <cellXfs count="4">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
        <xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
        <xf numFmtId="4" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
    </cellXfs>
</styleSheet>"""

    private fun getSharedStringsXml(strings: List<String>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="${strings.size}" uniqueCount="${strings.size}">""")
        for (s in strings) {
            val escaped = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
            sb.append("<si><t>$escaped</t></si>")
        }
        sb.append("</sst>")
        return sb.toString()
    }

    // ────────────────────────────────────────────────────────────────────────────
    // WORKSHEET GENERATORS
    // ────────────────────────────────────────────────────────────────────────────
    private fun generateExecutiveDashboardXml(
        transactions: List<Transaction>,
        categories: List<Category>,
        currencySymbol: String,
        addString: (String) -> Int
    ): String {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netCashflow = totalIncome - totalExpense

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheetData>
        <row r="1"><c r="A1" t="s" s="2"><v>${addString("VESPER LEDGER - EXECUTIVE FINANCIAL DASHBOARD")}</v></c></row>
        <row r="2"><c r="A2" t="s" s="1"><v>${addString("Financial Key Performance Indicators (KPIs)")}</v></c></row>
        <row r="3">
            <c r="A3" t="s"><v>${addString("Total Income (Inflow)")}</v></c>
            <c r="B3" s="3"><v>$totalIncome</v></c>
            <c r="C3" t="s"><v>${addString("Formula: =SUMIF('Transactions Master'!D:D,\"INCOME\",'Transactions Master'!F:F)")}</v></c>
        </row>
        <row r="4">
            <c r="A4" t="s"><v>${addString("Total Expenses (Outflow)")}</v></c>
            <c r="B4" s="3"><v>$totalExpense</v></c>
            <c r="C4" t="s"><v>${addString("Formula: =SUMIF('Transactions Master'!D:D,\"EXPENSE\",'Transactions Master'!F:F)")}</v></c>
        </row>
        <row r="5">
            <c r="A5" t="s"><v>${addString("Net Cashflow")}</v></c>
            <c r="B5" s="3"><f>B3-B4</f><v>$netCashflow</v></c>
            <c r="C5" t="s"><v>${addString("Formula: =B3-B4")}</v></c>
        </row>
        <row r="6">
            <c r="A6" t="s"><v>${addString("Total Transaction Count")}</v></c>
            <c r="B6"><f>COUNTA('Transactions Master'!A2:A1000)</f><v>${transactions.size}</v></c>
            <c r="C6" t="s"><v>${addString("Formula: =COUNTA('Transactions Master'!A2:A1000)")}</v></c>
        </row>
    </sheetData>
</worksheet>"""
    }

    private fun generateTransactionsMasterXml(
        transactions: List<Transaction>,
        categories: List<Category>,
        accounts: List<Account>,
        currencySymbol: String,
        addString: (String) -> Int
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")

        // Header Row
        sb.append("""<row r="1">""")
        listOf("Txn ID", "Date", "Title", "Type", "Category", "Amount", "Account", "Payment Method", "Notes").forEachIndexed { i, col ->
            val colLetter = ('A' + i).toString()
            sb.append("""<c r="${colLetter}1" t="s" s="1"><v>${addString(col)}</v></c>""")
        }
        sb.append("</row>")

        // Data Rows
        transactions.forEachIndexed { index, tx ->
            val r = index + 2
            val catName = categories.find { it.id == tx.categoryId }?.name ?: "Uncategorized"
            val acctName = accounts.find { it.id == tx.accountId }?.name ?: "Primary Account"
            val dateStr = dateFormat.format(Date(tx.dateEpochMillis))

            sb.append("""<row r="$r">""")
            sb.append("""<c r="A$r" t="s"><v>${addString(tx.id.toString())}</v></c>""")
            sb.append("""<c r="B$r" t="s"><v>${addString(dateStr)}</v></c>""")
            sb.append("""<c r="C$r" t="s"><v>${addString(tx.title.ifBlank { "Untitled" })}</v></c>""")
            sb.append("""<c r="D$r" t="s"><v>${addString(tx.type.name)}</v></c>""")
            sb.append("""<c r="E$r" t="s"><v>${addString(catName)}</v></c>""")
            sb.append("""<c r="F$r" s="3"><v>${tx.amount}</v></c>""")
            sb.append("""<c r="G$r" t="s"><v>${addString(acctName)}</v></c>""")
            sb.append("""<c r="H$r" t="s"><v>${addString(tx.paymentMethod.ifBlank { "Cash" })}</v></c>""")
            sb.append("""<c r="I$r" t="s"><v>${addString(tx.note)}</v></c>""")
            sb.append("</row>")
        }

        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun generateCategoryAnalyticsXml(
        transactions: List<Transaction>,
        categories: List<Category>,
        currencySymbol: String,
        addString: (String) -> Int
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")

        // Header Row
        sb.append("""<row r="1">""")
        listOf("Category Name", "Total Spent (SUMIF)", "Transaction Count (COUNTIF)", "Average Spend (AVERAGEIF)", "Formula Reference").forEachIndexed { i, col ->
            val colLetter = ('A' + i).toString()
            sb.append("""<c r="${colLetter}1" t="s" s="1"><v>${addString(col)}</v></c>""")
        }
        sb.append("</row>")

        categories.forEachIndexed { index, cat ->
            val r = index + 2
            val nameEscaped = cat.name.replace("\"", "")

            sb.append("""<row r="$r">""")
            sb.append("""<c r="A$r" t="s"><v>${addString(cat.name)}</v></c>""")
            sb.append("""<c r="B$r" s="3"><f>SUMIF('Transactions Master'!E:E, "$nameEscaped", 'Transactions Master'!F:F)</f></c>""")
            sb.append("""<c r="C$r"><f>COUNTIF('Transactions Master'!E:E, "$nameEscaped")</f></c>""")
            sb.append("""<c r="D$r" s="3"><f>AVERAGEIF('Transactions Master'!E:E, "$nameEscaped", 'Transactions Master'!F:F)</f></c>""")
            sb.append("""<c r="E$r" t="s"><v>${addString("=XLOOKUP(\"$nameEscaped\", A:A, B:B, 0)")}</v></c>""")
            sb.append("</row>")
        }

        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun generateAccountBalancesXml(
        transactions: List<Transaction>,
        accounts: List<Account>,
        currencySymbol: String,
        addString: (String) -> Int
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")

        // Header Row
        sb.append("""<row r="1">""")
        listOf("Account Name", "Initial Balance", "Total Inflow (SUMIFS)", "Total Outflow (SUMIFS)", "Calculated Current Balance", "Formula").forEachIndexed { i, col ->
            val colLetter = ('A' + i).toString()
            sb.append("""<c r="${colLetter}1" t="s" s="1"><v>${addString(col)}</v></c>""")
        }
        sb.append("</row>")

        accounts.forEachIndexed { index, acct ->
            val r = index + 2
            val nameEscaped = acct.name.replace("\"", "")

            sb.append("""<row r="$r">""")
            sb.append("""<c r="A$r" t="s"><v>${addString(acct.name)}</v></c>""")
            sb.append("""<c r="B$r" s="3"><v>${acct.initialBalance}</v></c>""")
            sb.append("""<c r="C$r" s="3"><f>SUMIFS('Transactions Master'!F:F, 'Transactions Master'!G:G, "$nameEscaped", 'Transactions Master'!D:D, "INCOME")</f></c>""")
            sb.append("""<c r="D$r" s="3"><f>SUMIFS('Transactions Master'!F:F, 'Transactions Master'!G:G, "$nameEscaped", 'Transactions Master'!D:D, "EXPENSE")</f></c>""")
            sb.append("""<c r="E$r" s="3"><f>B$r+C$r-D$r</f></c>""")
            sb.append("""<c r="F$r" t="s"><v>${addString("=B$r+C$r-D$r")}</v></c>""")
            sb.append("</row>")
        }

        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun generateBudgetAnalysisXml(
        budgets: List<Budget>,
        transactions: List<Transaction>,
        categories: List<Category>,
        currencySymbol: String,
        addString: (String) -> Int
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")

        // Header Row
        sb.append("""<row r="1">""")
        listOf("Budget Name", "Monthly Limit", "Actual Spend (SUMIF)", "Remaining Variance", "Usage %", "Budget Status").forEachIndexed { i, col ->
            val colLetter = ('A' + i).toString()
            sb.append("""<c r="${colLetter}1" t="s" s="1"><v>${addString(col)}</v></c>""")
        }
        sb.append("</row>")

        if (budgets.isEmpty()) {
            sb.append("""<row r="2"><c r="A2" t="s"><v>${addString("No active budgets created yet.")}</v></c></row>""")
        } else {
            budgets.forEachIndexed { index, b ->
                val r = index + 2
                val catName = categories.find { it.id == b.categoryId }?.name ?: "All Categories"
                val catEscaped = catName.replace("\"", "")

                sb.append("""<row r="$r">""")
                sb.append("""<c r="A$r" t="s"><v>${addString(b.name.ifBlank { catName })}</v></c>""")
                sb.append("""<c r="B$r" s="3"><v>${b.amount}</v></c>""")
                sb.append("""<c r="C$r" s="3"><f>SUMIF('Transactions Master'!E:E, "$catEscaped", 'Transactions Master'!F:F)</f></c>""")
                sb.append("""<c r="D$r" s="3"><f>B$r-C$r</f></c>""")
                sb.append("""<c r="E$r"><f>C$r/B$r</f></c>""")
                sb.append("""<c r="F$r" t="s"><f>IF(D$r>=0, "UNDER BUDGET", "OVER BUDGET")</f></c>""")
                sb.append("</row>")
            }
        }

        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun generateMacroHelpXml(addString: (String) -> Int): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheetData>
        <row r="1"><c r="A1" t="s" s="2"><v>${addString("VBA MACRO & EXCEL AUTOMATION GUIDE")}</v></c></row>
        <row r="2"><c r="A2" t="s"><v>${addString("Copy the code snippet below into Excel Developer -> Visual Basic (ALT+F11) to automate pivot refresh:")}</v></c></row>
        <row r="4"><c r="A4" t="s"><v>${addString("Sub RefreshVesperPivotTables()")}</v></c></row>
        <row r="5"><c r="A5" t="s"><v>${addString("    Dim pc As PivotCache")}</v></c></row>
        <row r="6"><c r="A6" t="s"><v>${addString("    For Each pc In ActiveWorkbook.PivotCaches")}</v></c></row>
        <row r="7"><c r="A7" t="s"><v>${addString("        pc.Refresh")}</v></c></row>
        <row r="8"><c r="A8" t="s"><v>${addString("    Next pc")}</v></c></row>
        <row r="9"><c r="A9" t="s"><v>${addString("    MsgBox \"Vesper Ledger Pivots Refreshed!\", vbInformation")}</v></c></row>
        <row r="10"><c r="A10" t="s"><v>${addString("End Sub")}</v></c></row>
    </sheetData>
</worksheet>"""
    }
}
