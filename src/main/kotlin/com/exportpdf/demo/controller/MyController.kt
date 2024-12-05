package com.exportpdf.demo.controller

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.jsoup.nodes.Document
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files


data class Company(
    val name: String,
    val street: String,
    val city: String,
    val zipCode: String,
    val country: String,
    val ico: String,
    val dic: String,
    val logo: String?
)

data class SingleInvoiceItem(
    val title: String,
    val quantity: String,
    val price: String,
    val vat: String?,
    val total: String,
)

@RestController
class MyController {
    @RequestMapping("/view")
    fun viewHtml(): String {
        /**
        Mocks
         */
        val company1 = Company(
            name = "Timoty s.r.o",
            street = "Opletalova 4",
            city = "Praha",
            zipCode = "110 00",
            country = "Ceska republika",
            ico = "12345678",
            dic = "CZ123456789",
            logo = "img"
        )
        val company2 = Company(
            name = "Timoty s.r.o",
            street = "Opletalova 4",
            city = "Praha",
            zipCode = "110 00",
            country = "Ceska republika",
            ico = "12345678",
            dic = "CZ123456789",
            logo = null
        )

        val items = arrayOf(
            SingleInvoiceItem(
                title = "Doprava po Praze včetně všeho",
                quantity = "10 km",
                price = "10 Kč",
                vat = "21%",
                total = "120 Kč",
            ),
            SingleInvoiceItem(
                title = "Doprava po Praze včetně všeho",
                quantity = "10 km",
                price = "10 Kč",
                vat = "21%",
                total = "120 Kč",
            )
        )

        /**
        Mocks end
         */


        val cssPath = ClassPathResource("templates/style.css").file.toPath()
        val cssContent = Files.readString(cssPath)

        val htmlTemplatePath = ClassPathResource("templates/invoice-template.html").file.toPath()
        var htmlTemplate = Files.readString(htmlTemplatePath)

        htmlTemplate = htmlTemplate.replace("{{css}}", cssContent)
            .replace("{{header}}", headerHtml(1, 245))
            .replace("{{companiesSection}}", companiesSection(company1, company2))
            .replace(
                "{{detailsSection}}",
                detailsSection(start = "27.11.2024", end = "27.11.2024", address = "Opletalova 1535/4, 110 00 Praha, Česká republika")
            )
            .replace("{{itemsSection}}", itemsSection(hasVat = true, items = items))
            .replace("{{signaturesSection}}", signaturesSection())
            .replace("{{footer}}", footer())

        return htmlTemplate
    }

    @GetMapping("/pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun generatePdf(): ResponseEntity<ByteArray> {
        /**
        Mocks
         */
        val company1 = Company(
            name = "Timoty s.r.o",
            street = "Opletalova 4",
            city = "Praha",
            zipCode = "110 00",
            country = "Ceska republika",
            ico = "12345678",
            dic = "CZ123456789",
            logo = "img"
        )
        val company2 = Company(
            name = "Timoty s.r.o",
            street = "Opletalova 4",
            city = "Praha",
            zipCode = "110 00",
            country = "Ceska republika",
            ico = "12345678",
            dic = "CZ123456789",
            logo = null
        )

        val items = arrayOf(
            SingleInvoiceItem(
                title = "Doprava po Praze včetně všeho",
                quantity = "10 km",
                price = "10 Kč",
                vat = "21%",
                total = "120 Kč",
            ),
            SingleInvoiceItem(
                title = "Doprava po Praze včetně všeho",
                quantity = "10 km",
                price = "10 Kč",
                vat = "21%",
                total = "120 Kč",
            )
        )

        /**
        Mocks end
         */

        val cssPath = ClassPathResource("templates/style.css").file.toPath()
        val cssContent = Files.readString(cssPath)

        val htmlTemplatePath = ClassPathResource("templates/invoice-template.html").file.toPath()
        var htmlTemplate = Files.readString(htmlTemplatePath)

        htmlTemplate = htmlTemplate.replace("{{css}}", cssContent)
            .replace("{{header}}", headerHtml(1, 245))
            .replace("{{companiesSection}}", companiesSection(company1 = company1, company2 = company2))
            .replace(
                "{{detailsSection}}",
                detailsSection(start = "27.11.2024", end = "27.11.2024", address = "Opletalova 1535/4, 110 00 Praha, Česká republika")
            )
            .replace("{{itemsSection}}", itemsSection(hasVat = true, items = items))
            .replace("{{signaturesSection}}", signaturesSection())
            .replace("{{footer}}", footer())

        val document: Document = Jsoup.parse(htmlTemplate, "/", org.jsoup.parser.Parser.xmlParser())
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml)

        val outputStream = ByteArrayOutputStream()

        val fontFile = File("src/main/resources/fonts/PlusJakartaSans.ttf")
        val builder = PdfRendererBuilder()

        try {
            builder.useFont(fontFile, "PlusJakartaSans")
            builder.useFastMode()
            builder.withW3cDocument(W3CDom().fromJsoup(document), "/")
            builder.toStream(outputStream)
            builder.run()
        } finally {
            outputStream.close()
        }

        val pdfBytes = outputStream.toByteArray()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dynamic.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes)
    }
}

fun headerHtml(invoiceId: Int, jobId: Int): String {
    return """
        <div class="container header">
            <div class="row">
                <p class="text1 float-left">Zakázkový list číslo.${invoiceId}</p>
                <p class="text2 float-right">PRÁCE #${jobId}</p>
                <div class="clearfix"></div>
            </div>
        </div>
    """.trimIndent()
}

fun companiesSection(company1: Company, company2: Company): String {
    return """
        <div class="divider"></div>
        <div class="container companySection">
            <div class="row">
                ${companyColumn(false, company2)}
                ${companyColumn(true, company1)}
            </div>
             <div class="clearfix"></div>
        </div>
    """.trimIndent()
}

fun companyColumn(isSupplier: Boolean, company: Company): String {
    val label = if (isSupplier) "Dodavatel" else "Odběratel"
    val logo = if (company.logo != null) """<div class="logoContainer"><img src=""/></div>""" else ""

    return """
        <div class="company">
            <p class="label">${label}</p>
            <p class="companyName"><strong>${company.name}</strong></p>
            <p>${company.street}</p>
            <p>${company.city}</p>
            <p>${company.zipCode}</p>
            <p>${company.country}</p>
            <p>IČO: ${company.ico}</p>
            <div class="lastItem">            
                <p>DIČ: ${company.dic}</p>
                ${logo}
            </div>
        </div>
    """.trimIndent()
}

fun detailsSection(start: String, end: String, address: String): String {
    return """
        <div class="divider"></div>
        <div class="detailsSection">
            <div class="row">
              ${detailColumn("Začátek", start)}
              ${detailColumn("Konec", end)}
              ${detailColumn("Lokalita", address)}
            </div>
             <div class="clearfix"></div>
        </div>
    """.trimIndent()
}

fun detailColumn(label: String, value: String): String {
    return """
        <div class="detailColumn">
          <p class="label">${label}</p>
          <p>${value}</p>
        </div>
    """.trimIndent()
}

fun itemsSection(hasVat: Boolean, items: Array<SingleInvoiceItem>): String {
    val totalLabel = if (hasVat) "Celkem vč DPH" else "Celkem"
    val vatColumn = if (hasVat) """<th class="cell">
                                        <p class="label">Sazba dph</p>
                                    </th>""".trimMargin() else ""

    return """
        <div class="divider"></div>
        <div class="itemsSection">
            <table>
              <tr>
                <th class="cell grow left"><p class="label">Položka</p></th>
                <th class="cell"><p class="label">Množství</p></th>
                <th class="cell"><p class="label">Cena za mj</p></th>
                 ${vatColumn}
                <th class="cell"><p class="label">${totalLabel}</p></th>
              </tr> 
                ${itemsContent(hasVat = hasVat, items = items)}
                ${totalSection(hasVat = hasVat, items = items)}
             </table>
        </div>
    """.trimIndent()
}

fun itemsContent(hasVat: Boolean, items: Array<SingleInvoiceItem>): String {
    val vatColumn = if (hasVat) """<td class="cell"><p>21 %</p></td>""" else ""
    var content = ""

    items.forEach {
        content += """
           <tr class="row itemsRow">
                <td class="cell grow left">
                <p>${it.title}</p>
                </td>
                <td class="cell">
                    <p>${it.quantity}</p>
                </td>
                <td class="cell">
                    <p>${it.price}</p>
                </td>
                $vatColumn
                <td class="cell">
                    <p>${it.total}</p>
                </td>
            </tr>
            """.trimIndent()
    }

    return """
            $content
    """.trimIndent()
}

fun totalSection(hasVat: Boolean, items: Array<SingleInvoiceItem>): String {
    val vatLabelCell = if (hasVat) """<td class="cell"> 
                                 <p class="label">DPH</p>
                            </td>""" else ""
    val vatTotalCell = if (hasVat) """<td class="cell">
                                        <p class="bold">500 Kč</p>
                                    </td>""".trimMargin() else ""
    val totalLabel = if (hasVat) "Celkem vč DPH" else "Celkem"

    return """
                <tr>
                    <td colspan="5" class="extraDivider">
                    </td>
                </tr>
                <tr class="totalSectionTop">
                    <td class="cell grow"></td>
                    <td class="cell grow"></td>
                    <td class="cell">
                         <p class="label">Cena</p>
                    </td>
                    $vatLabelCell
                    <td class="cell">
                         <p class="label">${totalLabel}</p>
                    </td>
                </tr>
                
                <tr class="totalSection">
                    <td class="cell grow"></td>
                    <td class="cell grow"></td>
                    <td class="cell">
                         <p class="bold">200 Kč</p>
                    </td>
                    $vatTotalCell
                     <td class="cell">
                         <p class="bold">2541 Kč</p>
                    </td>
                </tr>
    """.trimIndent()
}

fun signaturesSection(): String {
    return """
        <div class="divider"></div>
        <div class="container signaturesSection">
            <div class="row">
                  <div class="signatureColumn first">
                    <p class="label">Podpis za dodavatele</p>
                    <div class="signatureContainer"></div>
                    <p class="name">Timoty Bohaty</p>
                  </div>
                  <div class="signatureColumn">
                    <p class="label">Podpis za odběratele</p>
                    <div class="signatureContainer"></div>
                    <p class="name">Petr Pavel</p>
                  </div>
            </div>
            <div class="clearfix"></div>
        </div>
    """.trimIndent()
}

fun footer(): String {
    return """
        <div class="divider"></div>
        <div class="container footer">
          <p class="footerText">Dokument byl vytvořen a podepsán: 27.11.2024 (16:34) pomocí aplikace Timoty.io</p>
        </div>
    """.trimIndent()
}
