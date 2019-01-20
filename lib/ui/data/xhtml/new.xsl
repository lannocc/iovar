<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xxx="http://www.w3.org/1999/XSL/XXXTransformXXX"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    >

    <xsl:output
        method="xml"
        media-type="text/xml"
        omit-xml-declaration="no"
        indent="yes"
        />

    <xsl:template match="/">
        <xxx:stylesheet version="2.0">

            <xxx:output
                method="xml"
                media-type="application/xhtml+xml"
                doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
                doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
                omit-xml-declaration="no"
                />

            <xsl:apply-templates/>

        </xxx:stylesheet>
    </xsl:template>

    <xsl:template match="/*">
        <xxx:template match="/*">
            <html>
                <head>
                    <title>
                        New <xxx:value-of select="local-name()"/>
                    </title>
                    <link rel="stylesheet" type="text/css" href="/usr/include/iovar/ui/edit.css"/>
                    <script language="javascript" type="text/javascript" src="/usr/include/iovar/ui/general.js">/* */</script>
                    <script language="javascript" type="text/javascript" src="/usr/include/iovar/ui/edit.js">/* */</script>
                </head>

                <body>
                    <h1>
                        New <xxx:value-of select="local-name()"/>
                    </h1>


                    <form method="POST" class="edit" action="/$/bin/data/{{local-name()}}/mk" onsubmit="return !submits.push (this)">
                        <xxx:if test="/artist|/album|/track|/show|/episode|/ad">
                            <xxx:attribute name="enctype">multipart/form-data</xxx:attribute>
                        </xxx:if>

                        <ul class="fields">
                            <xxx:apply-templates/>

                            <xxx:if test="/album|/show|/episode|/ad">
                                <li>
                                    <label>Image:<xxx:text> </xxx:text>
                                        <input type="file" name="image"/>
                                    </label>
                                    <br/><a target="image" onclick="return !loadInPopup(this)" href="/pub/images/{{local-name(/*)}}s/{{id}}"><img src="/pub/images/{{local-name(/*)}}s/{{id}}" style="width:200px"/></a>
                                </li>
                            </xxx:if>
                        </ul>

                        <div class="buttons">
                            <input type="submit" class="submit" value="Create"/>
                            <noscript><input type="reset" class="cancel" value="Cancel"/></noscript>
                        </div>
                    </form>
                </body>
            </html>
        </xxx:template>

        <xxx:template match="id">
        </xxx:template>

        <xxx:template match="deleted|reg_address|mail_address">
            <input type="hidden" name="{{local-name()}}" value="{{.}}"></input>
        </xxx:template>

        <xxx:template match="Notes|Description">
            <li>
                <label><span class="label"><xxx:value-of select="local-name()"/>:</span><xxx:text> </xxx:text><xxx:text disable-output-escaping="yes">&lt;textarea name="</xxx:text><xxx:value-of select="local-name()"/><xxx:text disable-output-escaping="yes">"&gt;</xxx:text><xxx:value-of select="."/><xxx:text disable-output-escaping="yes">&lt;/textarea&gt;</xxx:text></label>
            </li>
        </xxx:template>

        <xxx:template match="customer-list/customer">
            <xxx:choose>
                <xxx:when test="deleted = 'Y'"/>
                <xxx:otherwise>
                    <option value="{{id}}">
                        <xxx:if test="../@id=id"><xxx:attribute name="selected">selected</xxx:attribute></xxx:if>
                        <xxx:if test="Company != ''">
                            <xxx:value-of select="Company"/>
                            <xxx:if test="Last-Name != '' or First-Name != ''">:<xxx:text> </xxx:text></xxx:if>
                        </xxx:if>
                        <xxx:if test="Last-Name != ''">
                            <xxx:value-of select="Last-Name"/>
                            <xxx:if test="First-Name != ''">,<xxx:text> </xxx:text></xxx:if>
                        </xxx:if>
                        <xxx:value-of select="First-Name"/>
                    </option>
                </xxx:otherwise>
            </xxx:choose>
        </xxx:template>

        <xxx:template match="part-list/part">
            <xxx:choose>
                <xxx:when test="deleted = 'Y'"/>
                <xxx:otherwise>
                    <option value="{{id}}">
                        <xxx:if test="../@id=id"><xxx:attribute name="selected">selected</xxx:attribute></xxx:if>
                        <xxx:value-of select="Mfg-Part-Num"/><xxx:text> </xxx:text>
                        <xxx:if test="Description != ''">
                            - <xxx:value-of select="Description"/>
                        </xxx:if>
                    </option>
                </xxx:otherwise>
            </xxx:choose>
        </xxx:template>

        <xxx:template match="part[@id]">
            <input type="hidden" name="{{local-name()}}" value="{{@id}}"/>
        </xxx:template>



        <!-- *-list -->
        <xxx:template match="*[substring (local-name(), string-length(local-name())-4)='-list']">
            <li>
                <label><span class="label">
                        <xxx:value-of select="translate (substring (local-name(), 1, 1), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xxx:value-of select="substring (local-name(), 2, string-length (local-name()) - 6)"/>
                        <xxx:text>: </xxx:text>
                    </span>
                    <select name="{{substring (local-name(), 1, string-length (local-name()) - 5)}}">
                        <option value="">--none--</option>
                        <xxx:apply-templates/>
                    </select>
                </label>
            </li>
        </xxx:template>

        <!-- *-list/* (record) -->
        <xxx:template match="*[substring (local-name(), string-length(local-name())-4)='-list']/*" priority="-1">
            <xxx:choose>
                <xxx:when test="deleted = 'Y'"/>
                <xxx:otherwise>
                    <option value="{{id}}">
                        <xxx:if test="../@id=id"><xxx:attribute name="selected">selected</xxx:attribute></xxx:if>
                        <xxx:if test="Code">
                            <xxx:value-of select="Code"/>:<xxx:text> </xxx:text>
                        </xxx:if>
                        <xxx:value-of select="Name"/>
                    </option>
                </xxx:otherwise>
            </xxx:choose>
        </xxx:template>



        <xxx:template match="*" priority="-5">
            <li>
                <label><span class="label"><xxx:value-of select="@tag-name-orig"/>:</span><xxx:text> </xxx:text>
                    <input type="text" name="{{@tag-name-orig}}" value="{{.}}"/>
                </label>
            </li>
        </xxx:template>

    </xsl:template>

</xsl:stylesheet>

