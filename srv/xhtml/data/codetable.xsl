<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    >

    <xsl:output
        method="xml"
        media-type="application/xhtml+xml"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
        omit-xml-declaration="no"
        />

    <!-- /*-list -->
    <xsl:template match="/*[substring (local-name(), string-length(local-name())-4)='-list']" priority="5">
        <html>
            <head>
                <title>IOVAR Demo - <xsl:value-of select="substring(local-name(), 1, string-length(local-name())-5)"/>s</title>
                <meta name="viewport" content="width=device-width" />
                <link rel="stylesheet" type="text/css" href="/usr/include/iovar/ui/general.css"/>
            </head>

            <body>
                <h1>IOVAR Demo</h1>

                <ul id="user">
                    <li><a href="/home/index.shtml">Logout</a></li>
                </ul>

                <img id="header" src="/usr/include/header.png"/>

                <ul id="social">
                    <li><a target="facebook" href="http://facebook.com/LannoccTech" title="Visit us on Facebook"><img src="/usr/include/facebook.png"/></a></li>
                </ul>

                <ul id="menu">
                    <li><a class="current" href="/$/index">Demo</a></li>
                </ul>

                <div id="body">
                    <xsl:variable name="name"><xsl:value-of select="substring(local-name(), 1, string-length(local-name())-5)"/></xsl:variable>

                    <h2><xsl:value-of select="$name"/>s</h2>

                    <ul>
                        <xsl:if test="$name != 'condition' and $name != 'unit'"><li><a href="/$/bin/data/new?={$name}">[ add new <xsl:value-of select="$name"/> ]</a></li></xsl:if>
                        <xsl:apply-templates/>
                    </ul>

                    <sub id="footer">
                        <img src="/usr/include/footer.jpg"/><br/>
                        Copyright (C) 2011-2015 Lannocc Technologies<br/>
                        Design &amp; Hosting by <a target="lannocc" href="http://lannocc.com" title="Web Sites. Business IT. Computer Service. Located in Billings, Montana.">Lannocc Technologies</a>
                    </sub>
                </div>
            </body>
        </html>
    </xsl:template>

    <!-- /*-list/* (record) -->
    <xsl:template match="/*[substring (local-name(), string-length(local-name())-4)='-list']/*" priority="5">
        <li>
            <xsl:call-template name="record"/>
        </li>
    </xsl:template>

    <!-- /*-list/*/id -->
    <xsl:template match="/*[substring (local-name(), string-length(local-name())-4)='-list']/*/id" priority="5">
    </xsl:template>

    <!-- /*-list/*/Code -->
    <xsl:template match="/*[substring (local-name(), string-length(local-name())-4)='-list']/*/Code" priority="5">
        <xsl:value-of select="."/> - 
    </xsl:template>

    <!-- /*-list/*/* (column) -->
    <xsl:template match="/*[substring (local-name(), string-length(local-name())-4)='-list']/*/*" priority="4">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template name="record">
        <a href="/$/bin/data/view?={local-name()}&amp;={id}">
            <xsl:apply-templates/>
        </a>
    </xsl:template>



    <xsl:template match="/*">
        <html>
            <head>
                <title>Edit <xsl:value-of select="local-name()"/></title>
                <link rel="stylesheet" type="text/css" href="/usr/include/iovar/ui/general.css"/>
            </head>

            <body>
                <h1>IOVAR Demo</h1>

                <ul id="user">
                    <li><a href="/home/index.shtml">Logout</a></li>
                </ul>

                <img id="header" src="/usr/include/header.png"/>

                <ul id="social">
                    <li><a target="facebook" href="http://facebook.com/LannoccTech" title="Visit us on Facebook"><img src="/usr/include/facebook.png"/></a></li>
                </ul>

                <ul id="menu">
                    <li><a href="/$/index">Demo</a></li>
                </ul>

                <div id="body">
                    <h2>Edit <xsl:value-of select="local-name()"/></h2>

                    <!--
                    <a href="/$/bin/data/rm?={local-name()}&amp;={id}">delete</a>
                    -->

                    <form method="POST" action="/$/bin/data/mod?={local-name()}&amp;={id}">
                        <ul>
                            <xsl:apply-templates/>
                        </ul>

                        <input type="submit" value="Modify"/>
                        <input type="reset" onclick="window.history.back()" value="Cancel"/>
                    </form>

                    <sub id="footer">
                        <img src="/usr/include/footer.jpg"/><br/>
                        Copyright (C) 2011-2015 Lannocc Technologies<br/>
                        Design &amp; Hosting by <a target="lannocc" href="http://lannocc.com" title="Web Sites. Business IT. Computer Service. Located in Billings, Montana.">Lannocc Technologies</a>
                    </sub>
                </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="id">
        <input type="hidden" name="{local-name()}" value="{.}"></input>
    </xsl:template>

    <xsl:template match="Notes|Description">
        <li>
            <label><xsl:value-of select="local-name()"/>: <xsl:text disable-output-escaping="yes">&lt;textarea name="</xsl:text><xsl:value-of select="local-name()"/><xsl:text disable-output-escaping="yes">"&gt;</xsl:text><xsl:value-of select="."/><xsl:text disable-output-escaping="yes">&lt;/textarea&gt;</xsl:text></label>
        </li>
    </xsl:template>


    <xsl:template match="*">
        <li>
            <label><xsl:value-of select="@tag-name-orig"/>:<xsl:text> </xsl:text>
                <input type="text" name="{@tag-name-orig}" value="{.}"/>
            </label>
        </li>
    </xsl:template>

</xsl:stylesheet>

