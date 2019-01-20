<?xml version="1.0"?>
<!--
     Copyright (C) 2017 Virgo Venture, Inc.
     @%@~LICENSE~@%@
-->
<xsl:stylesheet version="1.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    >

    <xsl:output
        method="xml"
        media-type="application/xhtml+xml"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
        omit-xml-declaration="no"
        />

    <xsl:template match="/">
        <xsl:comment>

            Copyright (C) 2017 Virgo Venture, Inc.
            @%@~LICENSE~@%@

        </xsl:comment>
        <html>
            <xsl:apply-templates/>
        </html>
    </xsl:template>

    <!--
         Main template:
             <ui version="4.0"> ... </ui>
    -->
    <xsl:template match="/ui[@version='4.0']">
        <xsl:variable name="title" select="widget/property[@name='windowTitle']/string"/>
        <head>
            <title><xsl:value-of select="$title"/> | qtui</title>
            <link rel="stylesheet" type="text/css" href="/usr/include/iovar/qtui.css"/>
        </head>
        <body>
            <h1>qtui</h1>
            <xsl:apply-templates/>
        </body>
    </xsl:template>

    <xsl:template match="/*" priority="-1">
        <head>
            <title>Error</title>
        </head>
        <body>
            <h1>Error</h1>
            <xsl:choose>
                <xsl:when test="/ui">
                    <code>Unsupported ui file version: <em><xsl:value-of select="@version"/></em></code>
                </xsl:when>
                <xsl:otherwise>
                    <code>Input does not appear to be a QT5 .ui XML tree (expecting &lt;ui&gt; root node): <xsl:value-of select="local-name()"/></code>
                </xsl:otherwise>
            </xsl:choose>
        </body>
    </xsl:template>

    <xsl:template match="*" priority="-1">
        <!-- hide everything by default... bring out only what we support in other templates below -->
    </xsl:template>



    <!--
         WIDGETS
    -->

    <xsl:template match="widget">
        <div id="{@name}" class="qtui-widget {@class}">
            <xsl:call-template name="style"/>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="widget[@class='QMainWindow']">
        <div id="{@name}" class="qtui-widget {@class}">
            <xsl:call-template name="style"/>

            <xsl:if test="property[@name='windowTitle']">
                <h2><xsl:value-of select="property[@name='windowTitle']/string"/></h2>
            </xsl:if>

            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="widget[@class='QDialog']">
        <div id="{@name}" class="qtui-widget {@class}">
            <xsl:call-template name="style"/>

            <xsl:if test="property[@name='windowTitle']">
                <h2><xsl:value-of select="property[@name='windowTitle']/string"/></h2>
            </xsl:if>

            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="widget[@class='QMenuBar']">
        <navbar id="{@name}" class="qtui-widget {@class}">
            <xsl:call-template name="style"/>

            <ul>
                <xsl:apply-templates/>
            </ul>
        </navbar>
    </xsl:template>

    <xsl:template match="widget[@class='QMenu']">
        <li id="{@name}" class="qtui-widget {@class}">
            <xsl:call-template name="style"/>

            <a>
                <xsl:call-template name="hotkey"><xsl:with-param name="label" select="property[@name='title']/string"/></xsl:call-template>
            </a>

            <xsl:if test="widget[@class='QMenu'] | addaction">
                <ul>
                    <xsl:apply-templates/>

                    <xsl:for-each select="addaction">
                        <xsl:variable name="name" select="@name"/>
                        <xsl:variable name="node" select="//action[@name=$name]"/>
                        <li id="$node/@name" class="qtui-action">
                            <a>
                                <xsl:call-template name="hotkey"><xsl:with-param name="label" select="$node/property[@name='text']/string"/></xsl:call-template>
                            </a>
                        </li>
                    </xsl:for-each>
                </ul>
            </xsl:if>
        </li>
    </xsl:template>

    <xsl:template match="widget[@class='QLabel']">
        <p id="{@name}" class="qtui-widget {@class}">
            <xsl:call-template name="style"/>
            <xsl:value-of select="property[@name='text']/string"/>
        </p>
    </xsl:template>

    <xsl:template match="widget[@class='QPushButton']">
        <button id="{@name}" class="qtui-widget {@class}">
            <xsl:call-template name="style"/>
            <xsl:value-of select="property[@name='text']/string"/>
        </button>
    </xsl:template>

    <xsl:template match="widget[@class='QListWidget']">
        <select id="{@name}" class="qtui-widget {@class}" size="5">
            <xsl:call-template name="style"/>
        </select>
    </xsl:template>

    <xsl:template match="widget[@class='QTextEdit']">
        <textarea id="{@name}" class="qtui-widget {@class}">
            <xsl:call-template name="style"/>
            <xsl:if test="property[@name='readOnly']/bool[text()='true']">
                <xsl:attribute name="readonly">readonly</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="property[@name='placeholderText']/string"/>
        </textarea>
    </xsl:template>

    <xsl:template match="widget[@class='QLineEdit']">
        <input type="text" id="{@name}" class="qtui-widget {@class}">
            <xsl:call-template name="style"/>
        </input>
    </xsl:template>



    <!--
         LAYOUT & MISC.
    -->

    <xsl:template match="layout">
        <div id="{@name}" class="qtui-layout {@class}">
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="item">
        <div class="qtui-item">
            <xsl:if test="../@stretch">
                <xsl:call-template name="stretch">
                    <xsl:with-param name="stretch" select="../@stretch"/>
                    <xsl:with-param name="desired" select="count(preceding-sibling::*[name() = name(current())])"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="spacer">
        <div class="qtui-spacer">
            <xsl:text disable-output-escaping="yes">&amp;</xsl:text>nbsp;
        </div>
    </xsl:template>




    <!--
         UTILITIES
    -->

    <xsl:template name="style">
        <xsl:attribute name="style">
            <xsl:if test="@class != 'QMainWindow' and @class != 'QDialog'">
                <xsl:if test="property[@name='geometry']/rect/x">position:absolute;left:<xsl:value-of select="property[@name='geometry']/rect/x"/>px;</xsl:if>
                <xsl:if test="property[@name='geometry']/rect/y">position:absolute;top:<xsl:value-of select="property[@name='geometry']/rect/y"/>px;</xsl:if>
            </xsl:if>
            <xsl:if test="property[@name='geometry']/rect/width">width:<xsl:value-of select="property[@name='geometry']/rect/width"/>px;</xsl:if>
            <xsl:if test="property[@name='geometry']/rect/height">height:<xsl:value-of select="property[@name='geometry']/rect/height"/>px;</xsl:if>
            <xsl:if test="property[@name='sizeGripEnabled']/bool[text()!='false']">max-width:100%; max-height:100%;</xsl:if>
            <xsl:if test="property[@name='minimumSize']/size/width">min-width:<xsl:value-of select="property[@name='minimumSize']/size/width"/>px;</xsl:if>
            <xsl:if test="property[@name='minimumSize']/size/height">min-height:<xsl:value-of select="property[@name='minimumSize']/size/height"/>px;</xsl:if>
            <xsl:if test="property[@name='maximumSize']/size/width">max-width:<xsl:value-of select="property[@name='maximumSize']/size/width"/>px;</xsl:if>
            <xsl:if test="property[@name='maximumSize']/size/height">max-height:<xsl:value-of select="property[@name='maximumSize']/size/height"/>px;</xsl:if>

            <xsl:if test="property[@name='pixmap']/pixmap">
                background-image:url(/usr/include/qtui/<xsl:value-of select="substring(property[@name='pixmap']/pixmap,2)"/>);
                background-repeat: no-repeat;
                background-position: center center;
            </xsl:if>

            <xsl:if test="property[@name='palette']/palette/active/colorrole[@role='Text']/brush[@brushstyle='SolidPattern']/color">color: rgba(
                <xsl:value-of select="property[@name='palette']/palette/active/colorrole[@role='Text']/brush[@brushstyle='SolidPattern']/color/red"/>,
                <xsl:value-of select="property[@name='palette']/palette/active/colorrole[@role='Text']/brush[@brushstyle='SolidPattern']/color/green"/>,
                <xsl:value-of select="property[@name='palette']/palette/active/colorrole[@role='Text']/brush[@brushstyle='SolidPattern']/color/blue"/>,
                <xsl:value-of select="property[@name='palette']/palette/active/colorrole[@role='Text']/brush[@brushstyle='SolidPattern']/color/@alpha div 255.0"/>);
            </xsl:if>

            <xsl:if test="property[@name='palette']/palette/active/colorrole[@role='Base']/brush[@brushstyle='SolidPattern']/color">background-color: rgba(
                <xsl:value-of select="property[@name='palette']/palette/active/colorrole[@role='Base']/brush[@brushstyle='SolidPattern']/color/red"/>,
                <xsl:value-of select="property[@name='palette']/palette/active/colorrole[@role='Base']/brush[@brushstyle='SolidPattern']/color/green"/>,
                <xsl:value-of select="property[@name='palette']/palette/active/colorrole[@role='Base']/brush[@brushstyle='SolidPattern']/color/blue"/>,
                <xsl:value-of select="property[@name='palette']/palette/active/colorrole[@role='Base']/brush[@brushstyle='SolidPattern']/color/@alpha div 255.0"/>);
            </xsl:if>

            <xsl:if test="property[@name='font']/font/pointsize">font-size: <xsl:value-of select="property[@name='font']/font/pointsize"/>pt;</xsl:if>
            <xsl:if test="property[@name='font']/font/bold[text()='true']">font-weight: bold;</xsl:if>

            <xsl:if test="layout">
                display: flex;
            </xsl:if>

            <xsl:if test="property[@name='styleSheet']/string"><xsl:value-of select="property[@name='styleSheet']/string"/></xsl:if>
        </xsl:attribute>
    </xsl:template>

    <xsl:template name="stretch">
        <xsl:param name="stretch"/>
        <xsl:param name="desired"/>
        <xsl:param name="index" select="0"/>

        <xsl:if test="string-length($stretch) > 0">
            <xsl:choose>
                <xsl:when test="$index = $desired">
                    <xsl:variable name="value" select="substring-before(concat($stretch, ','), ',')"/>
                    <xsl:if test="$value != '0'">
                        <xsl:attribute name="style">flex-grow: <xsl:value-of select="$value"/></xsl:attribute>
                    </xsl:if>
                </xsl:when>

                <xsl:otherwise>
                    <xsl:call-template name="stretch">
                        <xsl:with-param name="stretch" select="substring-after($stretch, ',')"/>
                        <xsl:with-param name="desired" select="$desired"/>
                        <xsl:with-param name="index" select="$index + 1"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template name="hotkey">
        <xsl:param name="label"/>

        <xsl:choose>
            <xsl:when test="contains($label, '&amp;')">
                <xsl:variable name="pre" select="substring-before($label, '&amp;')"/>
                <xsl:variable name="key" select="substring($label, string-length($pre)+2, 1)"/>
                <xsl:variable name="post" select="substring($label, string-length($pre) + 3)"/>
                <xsl:value-of select="$pre"/><code><xsl:value-of select="$key"/></code><xsl:value-of select="$post"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$label"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>




</xsl:stylesheet>

