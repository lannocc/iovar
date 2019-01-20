<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    >

    <xsl:template match="/log-list">
        <h2>Activity Log</h2>

        <table>
            <tr>
                <th>Date &amp; Time</th>
                <th>User</th>
                <th>Activity</th>
            </tr>
            <xsl:apply-templates/>
        </table>
    </xsl:template>

    <xsl:template match="log">
        <tr>
            <td><xsl:value-of select="time"/></td>
            <td><xsl:value-of select="user"/></td>
            <td>
                <xsl:choose>
                    <xsl:when test="action = 10">created</xsl:when>
                    <xsl:when test="action = 20">modified</xsl:when>
                    <xsl:when test="action = 30">deleted</xsl:when>
                    <xsl:when test="action = 40">exported</xsl:when>
                    <xsl:otherwise>???</xsl:otherwise>
                </xsl:choose>
                <xsl:text> </xsl:text>
                <xsl:value-of select="table"/>
                <xsl:text> </xsl:text>
                <xsl:choose>
                    <xsl:when test="action = 40">data</xsl:when>
                    <xsl:otherwise><xsl:value-of select="row"/></xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="id"/>


    <xsl:template match="/*" priority="-1">
        <xsl:choose>
            <xsl:when test="substring (local-name(), string-length(local-name())-4)='-list'">
                <h2><xsl:value-of select="substring (local-name(), 1, string-length (local-name()) - 5)"/>s</h2>
                <xsl:call-template name="list">
                    <xsl:with-param name="id" select="'data'"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <h2><xsl:value-of select="local-name()"/></h2>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>



    <xsl:template name="table">
        <xsl:param name="name"/>
        <xsl:param name="nosearch"/>
        <xsl:param name="param"/>
        <xsl:param name="param2"/>
        <xsl:param name="continue">true</xsl:param>

        <xsl:variable name="extra"><xsl:if test="$param">?=<xsl:value-of select="$param"/><xsl:if test="$param2">&amp;=<xsl:value-of select="$param2"/></xsl:if></xsl:if></xsl:variable>
        <xsl:variable name="id"><xsl:value-of select="$name"/><xsl:if test="$param">_<xsl:value-of select="$param"/><xsl:if test="$param2">_<xsl:value-of select="$param2"/></xsl:if></xsl:if></xsl:variable>

        <fieldset class="data {$name}s">
            <legend>
                <h3>
                    <a href="/$/bin/data/{$name}/ls{$extra}">
                        <xsl:attribute name="onclick"><xsl:choose>
                                <xsl:when test="$nosearch"/>
                                <xsl:otherwise>get ('<xsl:value-of select="$id"/>_search').value='';</xsl:otherwise>
                        </xsl:choose> return !loadToggle (this, '<xsl:value-of select="$id"/>') </xsl:attribute>
                        <xsl:value-of select="$name"/>s
                    </a>
                </h3>
            </legend>
            <form action="/$/bin/data/{$name}/find" method="GET" autocomplete="off" onsubmit="return false">
                <xsl:if test="$param">
                    <input type="hidden" name="param" value="{$param}"/>
                    <xsl:if test="$param2">
                        <input type="hidden" name="param2" value="{$param2}"/>
                    </xsl:if>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="$nosearch"/>
                    <xsl:otherwise>
                        <input id="{$id}_search" name="query" class="search" placeholder="search" autocomplete="off" onkeyup="loadSearch (event, this, '{$id}', '/$/bin/data/{$name}/find{$extra}')"/><noscript><input type="submit" value="Find"/></noscript>
                    </xsl:otherwise>
                </xsl:choose>
            </form>
            <div id="{$id}" class="content"/>
            <a class="create" target="{$name}" href="/$/bin/data/{$name}/new{$extra}" onclick="return !loadInPopup (this)"><span class="legacy">[</span>new <xsl:value-of select="$name"/><span class="legacy">]</span></a><br class="legacy"/>
            <xsl:if test="$continue = 'true'">
                <xsl:apply-templates/>
            </xsl:if>
        </fieldset>
    </xsl:template>

    <!-- *-list -->
    <xsl:template match="*[substring (local-name(), string-length(local-name())-4)='-list']" priority="-5">
        <xsl:call-template name="list"/>
    </xsl:template>

    <xsl:template name="list">
        <xsl:param name="id"/>
        <xsl:param name="ordered"/>

        <xsl:variable name="tag">
            <xsl:choose>
                <xsl:when test="$ordered = 'true'">ol</xsl:when>
                <xsl:otherwise>ul</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:element name="{$tag}">
            <xsl:if test="$id != ''"><xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute></xsl:if>
            <xsl:variable name="name" select="substring (local-name(), 1, string-length (local-name()) - 5)"/>
            <li class="create">
                <a target="{$name}" onclick="return !loadInPopup (this)">
                    <xsl:attribute name="href">/$/bin/data/<xsl:value-of select="$name"/>/new<xsl:if test="$arg1">?=<xsl:value-of select="$arg1"/><xsl:if test="$arg2">&amp;=<xsl:value-of select="$arg2"/></xsl:if></xsl:if></xsl:attribute>
                    <span class="legacy">[</span>new <xsl:value-of select="$name"/><span class="legacy">]</span>
                </a>
            </li>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <!-- *-list/* (record) -->
    <xsl:template match="*[substring (local-name(), string-length(local-name())-4)='-list']/*" priority="-5">
        <li>
            <xsl:call-template name="record"/>
        </li>
    </xsl:template>

    <xsl:template name="record">
        <a target="{local-name()}" href="/$/bin/data/{local-name()}/view?={id}" onclick="return !loadInPopup (this)">
            <xsl:apply-templates/>
        </a>
    </xsl:template>

    <!-- *-list/*/* (column) -->
    <xsl:template match="*[substring (local-name(), string-length(local-name())-4)='-list']/*/*" priority="-1">
        <xsl:apply-templates/>
    </xsl:template>

</xsl:stylesheet>

