<?xml version="1.0"?>
<!--
     Copyright (C) 2018-2019 Virgo Venture, Inc.
     @%@~LICENSE~@%@
-->
<xsl:stylesheet version="1.1"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:doc="http://docbook.org/ns/docbook"
    xmlns:iovar="http://iovar.net/net.iovar.web.Utils"
    >

    <xsl:import href="/lib/iovar/dates.xsl"/>
    <xsl:import href="/lib/iovar/xml.xsl"/>
    <xsl:import href="/lib/iovar/js.xsl"/>

    <!-- for Utils.get () -->
    <xsl:script implements-prefix="iovar" language="java" src="java:net.iovar.web.Utils"/>

    <!-- source file path (if known) -->
    <xsl:param name="source"/>

    <xsl:variable name="title">
        <xsl:choose>
            <xsl:when test="/doc:book/doc:info/doc:title"><xsl:value-of select="/doc:book/doc:info/doc:title"/></xsl:when>
            <xsl:when test="/doc:part/doc:title"><xsl:value-of select="/doc:part/doc:title"/></xsl:when>
            <xsl:when test="/doc:chapter/doc:title"><xsl:value-of select="/doc:chapter/doc:title"/></xsl:when>
            <xsl:when test="/doc:chapter/doc:info/doc:title"><xsl:value-of select="/doc:chapter/doc:info/doc:title"/></xsl:when>
            <xsl:when test="/doc:article/doc:title"><xsl:value-of select="/doc:article/doc:title"/></xsl:when>
            <xsl:when test="/doc:article/doc:info/doc:title"><xsl:value-of select="/doc:article/doc:info/doc:title"/></xsl:when>
            <xsl:when test="/doc:refentry"><xsl:choose>
                    <xsl:when test="//doc:cmdsynopsis[@role='ant']">Ant Target</xsl:when>
                    <xsl:when test="//doc:cmdsynopsis[@role='iosh']">Shell Command</xsl:when>
                    <xsl:when test="//doc:cmdsynopsis[@role='xsl']">XSL Template</xsl:when>
                    <xsl:when test="//doc:cmdsynopsis">Command</xsl:when>
                    <xsl:when test="//doc:funcsynopsis[@language='javascript']">JavaScript Function</xsl:when>
                    <xsl:otherwise>Reference</xsl:otherwise>
            </xsl:choose>: <xsl:value-of select="/doc:refentry/doc:refnamediv/doc:refname"/></xsl:when>
            <xsl:when test="/doc:index/doc:title"><xsl:value-of select="/doc:index/doc:title"/></xsl:when>
            <xsl:when test="/doc:index/doc:info/doc:title"><xsl:value-of select="/doc:index/doc:info/doc:title"/></xsl:when>
            <xsl:otherwise>Web Development Platform</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name="desc">
        <xsl:choose>
            <xsl:when test="/doc:book/doc:preface"><xsl:value-of select="/doc:book/doc:preface/doc:para[1]"/></xsl:when>
            <xsl:when test="/doc:refentry"><xsl:choose>
                    <xsl:when test="//doc:cmdsynopsis[@role='ant']">Ant Target</xsl:when>
                    <xsl:when test="//doc:cmdsynopsis[@role='iosh']">Shell Command</xsl:when>
                    <xsl:when test="//doc:cmdsynopsis[@role='xsl']">XSL Template</xsl:when>
                    <xsl:when test="//doc:cmdsynopsis">Command</xsl:when>
                    <xsl:when test="//doc:funcsynopsis[@language='javascript']">JavaScript Function</xsl:when>
                    <xsl:otherwise>Reference</xsl:otherwise>
            </xsl:choose>: <xsl:value-of select="/doc:refentry/doc:refnamediv/doc:refname"/> | <xsl:value-of select="/doc:refentry/doc:refnamediv/doc:refpurpose"/></xsl:when>
            <xsl:otherwise>IOVAR Web Platform</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name="modules" select="document('/doc/toc.xml')"/>
    <xsl:variable name="modentries" select="$modules//doc:tocentry/doc:link"/>

    <xsl:variable name="module">
        <xsl:if test="starts-with($source, '/doc/')">
            <xsl:value-of select="substring-before(substring($source, 6),'/')"/>
        </xsl:if>
    </xsl:variable>
    <xsl:variable name="relsource"><xsl:if test="starts-with($source, concat('/doc/',$module,'/'))"><xsl:value-of select="substring($source,7+string-length($module))"/></xsl:if></xsl:variable>

    <xsl:variable name="toc" select="document(concat('/doc/',$module,'/toc.xml'))"/>
    <xsl:variable name="tocentries" select="$toc//doc:tocentry/doc:link"/>

    <xsl:import href="/lib/xhtml/common.xsl"/>

    <!--
    <xsl:attribute-set name="body">
        <xsl:attribute name="class"><xsl:if test="/doc:book|/doc:part|/doc:chapter|/doc:article">doc</xsl:if></xsl:attribute>
    </xsl:attribute-set>
    -->

    <!-- this overrides the 'head' template from /lib/xhtml/common.xsl -->
    <xsl:template name="head">
        <xsl:if test="/doc:book|/doc:part|/doc:chapter|/doc:article|/doc:refentry|/doc:index">
            <xsl:choose>
                <xsl:when test="$source = concat('/doc/',$module,'/index.xml')">
                    <meta property="og:type" content="website"/>

                    <xsl:if test="$tocentries[1]">
                        <link rel="next" title="{normalize-space($tocentries[1])}" href="{$tocentries[1]/@xlink:href}"/>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <meta property="og:type" content="article"/>
                    <!-- FIXME: publisher actually needs to be a facebook PAGE -->
                    <meta property="article:publisher" content="https://www.facebook.com/groups/1897834283764139"/>
                    <xsl:if test="/*/doc:info/doc:author/doc:uri[@type='facebook']">
                        <meta property="article:author" content="{/*/doc:info/doc:author/doc:uri[@type='facebook']}"/>
                    </xsl:if>
                    <xsl:if test="/*/doc:info/doc:pubdate">
                        <meta property="article:published_time" content="{/*/doc:info/doc:pubdate}"/>
                    </xsl:if>
                    <xsl:if test="/*/doc:info/doc:revhistory">
                        <meta property="article:modified_time" content="{/*/doc:info/doc:revhistory/doc:revision[last()]/doc:date}"/>
                    </xsl:if>

                    <xsl:for-each select="$tocentries">
                        <xsl:variable name="index" select="position()"/>
                        <xsl:if test="@xlink:href = $relsource">
                            <xsl:if test="$index != last()">
                                <xsl:variable name="next" select="$tocentries[position() = $index + 1]"/>
                                <link rel="next" title="{normalize-space($next)}" href="/doc/{$module}/{$next/@xlink:href}"/>
                            </xsl:if>

                            <xsl:choose>
                                <xsl:when test="$index &gt; 1">
                                    <xsl:variable name="prev" select="$tocentries[position() = $index - 1]"/>
                                    <link rel="prev" title="{normalize-space($prev)}" href="/doc/{$module}/{$prev/@xlink:href}"/>
                                </xsl:when>
                            </xsl:choose>
                            <link rel="up" title="Documentation Home ({$module})" href="/doc/{$module}/"/>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>

            <link rel="canonical" href="https://iovar.net{$source}" title="(link to this page)"/>
            <meta property="og:url" content="https://iovar.net{$source}"/>

            <link rel="stylesheet" type="text/css" href="/usr/include/iovar/prism.css"/>
            <link rel="stylesheet" type="text/css" href="/usr/include/iovar/doc.css"/>

            <!--
            <script language="javascript" type="text/javascript" src="/usr/include/iovar/clipboard.min.js">/* */</script>
            -->
            <script language="javascript" type="text/javascript" src="/usr/include/iovar/prism.js">/* */</script>
        </xsl:if>
    </xsl:template>

    <xsl:template name="body">
        <nav id="docnav">
            <xsl:choose>
                <xsl:when test="$relsource = '' or $relsource = 'index' or $relsource = 'index.xml'">
                    <xsl:variable name="next" select="$tocentries[position() = 1]"/>
                    <a rel="next" title="Next: {normalize-space($next)}" href="/doc/{$module}/{$next/@xlink:href}"><img alt="next: " src="/usr/include/iovar/next.png"/><xsl:value-of select="$next"/></a>
                    <span class="legacy">|</span>
                    <a class="here" title="Documentation Home ({$module})" href="/doc/{$module}/"><img alt="up" src="/usr/include/iovar/up.png"/></a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="$tocentries">
                        <xsl:variable name="index" select="position()"/>
                        <xsl:if test="@xlink:href = $relsource">
                            <xsl:choose>
                                <xsl:when test="$index &gt; 1">
                                    <xsl:variable name="prev" select="$tocentries[position() = $index - 1]"/>
                                    <a rel="prev" title="Previous: {normalize-space($prev)}" href="/doc/{$module}/{$prev/@xlink:href}"><img alt="previous" src="/usr/include/iovar/previous.png"/></a>
                                </xsl:when>
                                <xsl:otherwise>
                                    <a rel="prev" title="Previous: Documentation Home ({$module})" href="/doc/{$module}/"><img alt="previous" src="/usr/include/iovar/previous.png"/></a>
                                </xsl:otherwise>
                            </xsl:choose>

                            <xsl:if test="$index != last()">
                                <xsl:variable name="next" select="$tocentries[position() = $index + 1]"/>
                                <span class="legacy">|</span>
                                <a rel="next" title="Next: {normalize-space($next)}" href="/doc/{$module}/{$next/@xlink:href}"><img alt="next: " src="/usr/include/iovar/next.png"/><xsl:value-of select="$next"/></a>
                            </xsl:if>

                            <span class="legacy">|</span>
                        </xsl:if>
                    </xsl:for-each>

                    <a rel="up" title="Documentation Home ({$module})" href="/doc/{$module}/"><img alt="up" src="/usr/include/iovar/up.png"/></a>
                </xsl:otherwise>
            </xsl:choose>
        </nav>

        <nav id="contents-select">
            <h3>Docs</h3>
            <ul>
                <xsl:for-each select="$modentries">
                    <li>
                        <a href="/doc/{@xlink:href}/"><xsl:choose>
                                <xsl:when test="$module = @xlink:href">
                                    <xsl:attribute name="rel">up</xsl:attribute>
                                    <xsl:attribute name="class">here</xsl:attribute>
                                    <em><xsl:value-of select="."/></em>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="."/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </a>
                    </li>
                </xsl:for-each>
            </ul>
        </nav>

        <main id="body">
            <xsl:apply-templates/>
        </main>
    </xsl:template>

    <xsl:template match="/*">
        <h2>Not Implemented</h2>
        <p>Node type: <em>&lt;<xsl:value-of select="name(.)"/> xmlns="<xsl:value-of select="namespace-uri(.)"/>"&gt;</em></p>
    </xsl:template>

    <xsl:template match="/error">
        <h2>Yikes!</h2>

        <p class="error">Something went wrong: <xsl:value-of select="."/></p>
    </xsl:template>


    <xsl:template match="/doc:book|/doc:part|/doc:chapter|/doc:article|/doc:refentry|/doc:index">
        <xsl:if test="$toc">
            <input class="show-dropdown" type="checkbox" id="show-contents" role="button"/>
            <label for="show-contents" title="Show/Hide Contents"><img alt="dropdown" src="/usr/include/iovar/dropdown.png"/></label>

            <nav id="contents" class="dropdown">
                <xsl:apply-templates select="$toc/*"/>
            </nav>
        </xsl:if>

        <article id="doc">
            <xsl:if test="doc:info/doc:author | doc:info/doc:pubdate">
                <div class="docmeta">
                    <xsl:choose>
                        <xsl:when test="doc:info/doc:revhistory"><xsl:apply-templates select="doc:info/doc:revhistory"/></xsl:when>
                        <xsl:otherwise><xsl:apply-templates select="doc:info/doc:pubdate"/></xsl:otherwise>
                    </xsl:choose>
                    <xsl:apply-templates select="doc:info/doc:author"/>
                </div>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="/doc:index">
                    <!--
                    <dl class="index">
                        <xsl:apply-templates/>
                    </dl>
                    -->
                    <xsl:call-template name="index"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </article>
    </xsl:template>

    <xsl:template match="doc:info|doc:title"/>

    <!-- a primary table of contents -->
    <xsl:template match="/doc:toc">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="doc:tocdiv">
        <div class="tocdiv">
            <h4><xsl:value-of select="doc:title"/></h4>
            <ul>
                <xsl:apply-templates/>
            </ul>
        </div>
    </xsl:template>

    <xsl:template match="doc:tocentry">
        <li><xsl:apply-templates/></li>
    </xsl:template>

    <!-- inline table of contents -->
    <xsl:template match="doc:toc">
        <h2 class="legacy">Contents</h2>

        <xsl:if test="doc:tocentry">
            <ol class="toc">
                <xsl:apply-templates/>
            </ol>
        </xsl:if>

        <xsl:if test="../doc:article">
            <ul class="toc articles">
                <xsl:for-each select="../doc:article">
                    <xsl:call-template name="tocentry">
                        <xsl:with-param name="node" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
            </ul>
        </xsl:if>

        <xsl:if test="../doc:section">
            <ol class="toc sections">
                <xsl:for-each select="../doc:section">
                    <xsl:call-template name="tocentry">
                        <xsl:with-param name="node" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
            </ol>
        </xsl:if>

        <xsl:if test="../doc:chapter">
            <ol class="toc chapters">
                <xsl:for-each select="../doc:chapter">
                    <xsl:call-template name="tocentry"/>
                </xsl:for-each>
            </ol>
        </xsl:if>

        <xsl:if test="../doc:appendix">
            <ol class="toc appendices">
                <xsl:for-each select="../doc:appendix">
                    <xsl:call-template name="tocentry"/>
                </xsl:for-each>
            </ol>
        </xsl:if>
    </xsl:template>

    <xsl:template name="tocentry">
        <li>
            <xsl:if test="self::doc:appendix"><sup>Appendix: </sup></xsl:if>
            <a href="#entry-{count(preceding-sibling::doc:*)}"><xsl:value-of select="doc:title"/></a>
        </li>
    </xsl:template>

    <xsl:template match="doc:preface">
        <div class="preface">
            <xsl:if test="doc:title/text()">
                <h2><xsl:value-of select="doc:title"/></h2>
            </xsl:if>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="doc:chapter">
        <hr class="legacy"/>
        <div class="chapter">
            <h2><a name="entry-{count(preceding-sibling::doc:*)}"><xsl:value-of select="doc:title"/></a></h2>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="doc:appendix">
        <hr class="legacy"/>
        <div class="appendix">
            <h2><a name="entry-{count(preceding-sibling::doc:*)}">Appendix<xsl:if test="doc:title">: <xsl:value-of select="doc:title"/></xsl:if></a></h2>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="doc:article">
        <hr class="legacy"/>
        <div class="article">
            <h3><a name="entry-{count(preceding-sibling::doc:*)}"><xsl:value-of select="doc:title|doc:info/doc:title"/></a></h3>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="doc:section|doc:refsection">
        <div class="section">
            <h4><a name="entry-{count(preceding-sibling::doc:*)}"><xsl:value-of select="doc:title"/></a></h4>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template name="index">
        <xsl:for-each select="doc:indexentry">
            <xsl:variable name="section" select="translate(substring(doc:primaryie,1,1),$STR_LOWER,$STR_UPPER)"/>
            <xsl:if test="not(preceding-sibling::*[translate(substring(doc:primaryie,1,1),$STR_LOWER,$STR_UPPER)=$section])">
                <h2><xsl:value-of select="$section"/></h2>

                <dl class="index">
                    <xsl:for-each select="../doc:indexentry[translate(substring(doc:primaryie,1,1),$STR_LOWER,$STR_UPPER)=$section]">
                        <xsl:apply-templates select="."/>
                    </xsl:for-each>
                </dl>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="doc:indexentry">
        <dt>
            <xsl:apply-templates select="doc:primaryie/text()[1] | doc:primaryie/doc:command"/>
        </dt>
        <dd>
            <xsl:apply-templates select="doc:primaryie/doc:link | doc:primaryie/text()[not(position() = 1)]"/>

            <xsl:if test="doc:secondaryie | doc:seeie | doc:seealsoie">
                <ul>
                    <xsl:for-each select="doc:secondaryie">
                        <li><xsl:apply-templates/></li>
                    </xsl:for-each>

                    <xsl:for-each select="doc:seeie">
                        <li class="see"><sup>see:</sup><xsl:text> </xsl:text><xsl:apply-templates/></li>
                    </xsl:for-each>

                    <xsl:for-each select="doc:seealsoie">
                        <li class="see"><sup>see also:</sup><xsl:text> </xsl:text><xsl:apply-templates/></li>
                    </xsl:for-each>
                </ul>
            </xsl:if>
        </dd>
    </xsl:template>

    <xsl:template match="doc:para">
        <xsl:choose>
            <xsl:when test="count(preceding-sibling::doc:*) = 0 and count(following-sibling::doc:*) = 0">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:otherwise>
                <p><xsl:apply-templates/></p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="doc:link">
        <xsl:variable name="this" select="concat('/doc/',$module,'/',@xlink:href)"/>
        <xsl:variable name="href"><xsl:choose>
                <!-- FIXME: ugly hack -->
                <xsl:when test="local-name(..) = 'tocentry' and local-name(../..) = 'tocdiv' and local-name(../../..) = 'toc' and ../../../.. = $toc"><xsl:value-of select="concat('/doc/',$module,'/',@xlink:href)"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="@xlink:href"/></xsl:otherwise>
        </xsl:choose></xsl:variable>

        <a href="{$href}"><xsl:if test="starts-with(@xlink:href, 'http://') or starts-with(@xlink:href, 'https://')"><xsl:attribute name="target">_new</xsl:attribute></xsl:if><xsl:if test="$this = $source"><xsl:attribute name="class">here</xsl:attribute></xsl:if>
            <xsl:choose>
                <xsl:when test="not(text()) and substring(@xlink:href, string-length(@xlink:href)) = '/'">
                    <xsl:value-of select="document(concat('res:/doc/iovar/',@xlink:href,'index.xml'))//doc:title"/>
                </xsl:when>
                <!--
                <xsl:otherwise><xsl:value-of select="document(concat('res:/doc/iovar/02.overview/',@xlink:href))//doc:title"/></xsl:otherwise>
                -->
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$this = $source">
                            <em><xsl:apply-templates/></em>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </a>
    </xsl:template>

    <xsl:template match="doc:olink">
        <a href="{@targetdoc}"><xsl:choose>
                <xsl:when test="text()"><xsl:value-of select="."/></xsl:when>
                <xsl:otherwise><xsl:value-of select="@targetdoc"/></xsl:otherwise>
        </xsl:choose></a>
    </xsl:template>

    <xsl:template match="doc:personname">
        <xsl:value-of select="doc:firstname"/><xsl:text> </xsl:text><xsl:value-of select="doc:surname"/>
    </xsl:template>

    <xsl:template match="doc:citetitle">
        "<xsl:apply-templates/>"
    </xsl:template>

    <xsl:template match="doc:citetitle[@pubwork='book']">
        <i><xsl:apply-templates/></i>
    </xsl:template>

    <xsl:template match="doc:itemizedlist">
        <ul>
            <xsl:apply-templates/>
        </ul>
    </xsl:template>

    <xsl:template match="doc:orderedlist">
        <ol>
            <xsl:apply-templates/>
        </ol>
    </xsl:template>

    <xsl:template match="doc:listitem">
        <li>
            <xsl:apply-templates/>
        </li>
    </xsl:template>

    <xsl:template match="doc:variablelist">
        <dl>
            <xsl:apply-templates/>
        </dl>
    </xsl:template>

    <xsl:template match="doc:term">
        <dt><xsl:apply-templates/></dt>
    </xsl:template>

    <xsl:template match="doc:varlistentry/doc:listitem">
        <dd><xsl:apply-templates/></dd>
    </xsl:template>

    <xsl:template match="doc:blockquote">
        <blockquote>
            <xsl:apply-templates select="doc:info/doc:pubdate"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="doc:info/doc:author"/>
        </blockquote>
    </xsl:template>

    <xsl:template match="doc:pubdate">
        <sup class="pubdate"><xsl:call-template name="date-text-from-digits"/></sup>
    </xsl:template>

    <xsl:template match="doc:revhistory">
        <xsl:apply-templates select="doc:revision[last()]"/>
    </xsl:template>

    <xsl:template match="doc:revision">
        <sup class="revision">
            <xsl:if test="../../doc:pubdate"><xsl:attribute name="title">First Published Here: <xsl:call-template name="date-text-from-digits">
                        <xsl:with-param name="date" select="../../doc:pubdate"/>
            </xsl:call-template></xsl:attribute></xsl:if>
            <xsl:apply-templates/> (updated)
        </sup>
    </xsl:template>

    <xsl:template match="doc:date">
        <span class="date"><xsl:call-template name="date-text-from-digits"/></span>
    </xsl:template>

    <xsl:template match="doc:author">
        <sub class="author">
            <xsl:if test="doc:personname and doc:affiliation">
                <xsl:attribute name="title">of <xsl:value-of select="normalize-space(doc:affiliation)"/></xsl:attribute>
            </xsl:if>
            by:
            <xsl:choose>
                <xsl:when test="doc:uri">
                    <a target="author" href="{doc:uri}" title="Visit Author Website"><xsl:choose>
                            <xsl:when test="doc:personname|doc:orgname"><xsl:apply-templates/></xsl:when>
                            <xsl:otherwise><xsl:value-of select="doc:uri"/></xsl:otherwise>
                        </xsl:choose>
                    </a>
                </xsl:when>
                <xsl:when test="doc:personname">
                    <xsl:apply-templates select="doc:personname"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </sub>
    </xsl:template>

    <xsl:template match="doc:uri"/>

    <xsl:template match="doc:tip">
        <em class="tip" title="Tip"><xsl:apply-templates/></em>
    </xsl:template>

    <xsl:template match="doc:important">
        <em class="important" title="Important"><xsl:apply-templates/></em>
    </xsl:template>

    <xsl:template match="doc:example">
        <div class="example">
            <em class="title"><sup>Example:</sup><xsl:text> </xsl:text><xsl:apply-templates select="doc:title/node()"/></em>
            <xsl:apply-templates/>
            <xsl:if test="*[position()=last()]/self::doc:screen">
                <div class="margin"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template match="doc:programlisting">
        <div class="listing">
            <xsl:if test="@xlink:href and @xlink:show = 'replace'">
                <em class="title"><sup>Listing:</sup><xsl:text> </xsl:text><a href="{@xlink:href}"><xsl:value-of select="substring-before(concat(@xlink:href,'#'),'#')"/></a><xsl:if test="contains(@xlink:href,'#')"><xsl:text> </xsl:text><sub>(fragment)</sub></xsl:if></em>
            </xsl:if>
            <pre><xsl:if test="@continuation = 'continues'"><xsl:attribute name="data-start"><xsl:call-template name="continue-programlisting"/></xsl:attribute></xsl:if><xsl:choose>
                    <xsl:when test="@language"><xsl:variable name="lines"><xsl:if test="not(contains(@xlink:href,'#'))"> line-numbers</xsl:if></xsl:variable><code class="language-{@language}{$lines}"><xsl:apply-templates select="@* | node()"/></code></xsl:when>
                    <xsl:otherwise><xsl:apply-templates select="@* | node()"/></xsl:otherwise>
            </xsl:choose></pre>
        </div>
    </xsl:template>

    <xsl:template name="continue-programlisting">
        <xsl:param name="node" select="."/>
        <xsl:param name="count" select="0"/>
        <xsl:choose>
            <xsl:when test="$node/preceding-sibling::doc:programlisting">
                <xsl:variable name="text" select="$node/preceding-sibling::doc:programlisting[1]/text()"/>
                <xsl:call-template name="continue-programlisting">
                    <xsl:with-param name="node" select="$node/preceding-sibling::doc:programlisting[1]"/>
                    <xsl:with-param name="count" select="$count + string-length($text) - string-length(translate($text,'&#10;',''))"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$count + 1"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="doc:programlisting[@language='terminal']">
        <div class="listing">
            <pre><xsl:if test="doc:prompt"><xsl:attribute name="data-prompt"><xsl:value-of select="normalize-space(doc:prompt)"/></xsl:attribute></xsl:if><xsl:if test="doc:computeroutput"><xsl:attribute name="data-filter-output">| </xsl:attribute></xsl:if><code class="command-line language-shell"><xsl:apply-templates/></code></pre>
        </div>
    </xsl:template>
    <xsl:template match="doc:programlisting[@language='terminal']/doc:prompt[1]"/>
    <xsl:template match="doc:programlisting[@language='terminal']/doc:userinput">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="doc:programlisting[@language='terminal']/doc:computeroutput">| <xsl:call-template name="terminal-prism-fix">
            <xsl:with-param name="text"><xsl:call-template name="string-replace-all">
                    <xsl:with-param name="text" select="."/>
                    <xsl:with-param name="replace" select="'&#10;'"/>
                    <xsl:with-param name="with" select="'&#10;| '"/>
            </xsl:call-template></xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--
         FIXME: Unfortunately, prism.js terminal mode does not handle a &lt; (less-than) entity properly.
                Our fix here somewhat messes up the plain-text/noscript output.
    -->
    <xsl:template name="terminal-prism-fix">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="contains($text, '&lt;')">
                <xsl:value-of select="substring-before($text,'&lt;')"/>&amp;lt;<xsl:call-template name="terminal-prism-fix">
                    <xsl:with-param name="text" select="substring-after($text,'&lt;')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="doc:screen">
        <div class="output">
            <em class="title"><sup>Output:</sup></em>
            <pre class="screen"><xsl:apply-templates/></pre>
        </div>
    </xsl:template>

    <xsl:template match="doc:code">
        <code><xsl:apply-templates/></code>
    </xsl:template>

    <xsl:template match="doc:literal">
        <tt class="literal"><xsl:apply-templates/></tt>
    </xsl:template>

    <xsl:template match="doc:replaceable">
        <tt class="replaceable"><xsl:apply-templates/></tt>
    </xsl:template>

    <xsl:template match="doc:tag[@class='element']">
        <tt class="element"><xsl:apply-templates/></tt>
    </xsl:template>

    <xsl:template match="doc:varname">
        <tt class="varname"><xsl:apply-templates/></tt>
    </xsl:template>

    <xsl:template match="doc:command">
        <xsl:variable name="this" select="."/>
        <xsl:for-each select="//doc:command">
            <xsl:if test="generate-id(.) = generate-id($this)">
                <tt class="command"><a name="command-{position()}"><xsl:apply-templates/></a></tt>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="doc:command[@role]">
        <xsl:variable name="this" select="."/>
        <xsl:for-each select="//doc:command">
            <xsl:if test="generate-id(.) = generate-id($this)">
                <tt class="command role_{@role}">
                    <a name="command-{position()}">
                        <xsl:if test="@role">
                            <xsl:attribute name="href">/doc/<xsl:value-of select="$module"/>/<xsl:value-of select="@role"/>/<xsl:value-of select="."/>.xml</xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
                    </a>
                </tt>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="doc:function">
        <xsl:variable name="this" select="."/>
        <xsl:for-each select="//doc:function">
            <xsl:if test="generate-id(.) = generate-id($this)">
                <tt class="function">
                    <a name="function-{position()}">
                        <xsl:if test="@role = 'javascript'">
                            <xsl:attribute name="href">/doc/<xsl:value-of select="$module"/>/js/<xsl:value-of select="."/>.xml</xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
                    </a>
                </tt>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="doc:filename">
        <tt class="filename {@class}"><xsl:apply-templates/></tt>
    </xsl:template>

    <xsl:template match="doc:remark">
        <em class="remark"><xsl:apply-templates/></em>
    </xsl:template>

    <xsl:template match="doc:refnamediv">
        <h2><tt><xsl:value-of select="doc:refname"/></tt></h2>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="doc:refname"/>
    <xsl:template match="doc:refpurpose">
        <div class="purpose">
            <h3>Purpose</h3>
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    <xsl:template match="doc:refsynopsisdiv">
        <div class="synopsis">
            <h3>Synopsis</h3>
            <dl>
                <xsl:apply-templates/>
            </dl>
        </div>
    </xsl:template>

    <xsl:template match="doc:funcsynopsis">
        <div class="function_synopsis language_{@language}">
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    <xsl:template match="doc:funcsynopsisinfo/doc:filename">
        <dt>location:</dt>
        <dd><tt class="filename"><xsl:apply-templates/></tt></dd>
    </xsl:template>
    <xsl:template match="doc:funcprototype">
        <dt>prototype:</dt>
        <dd><tt class="function">
                <span class="func_return" title="Return value type"><xsl:value-of select="doc:funcdef/doc:type"/></span>
                <xsl:text> </xsl:text>
                <span class="func_name" title="Function name"><xsl:value-of select="doc:funcdef/doc:function"/></span>
                (
                <xsl:for-each select="doc:paramdef">
                    <xsl:if test="position() &gt; 1">,<xsl:text> </xsl:text></xsl:if>
                    <span class="func_param_type" title="Argument type"><xsl:value-of select="doc:type"/></span>
                    <xsl:text> </xsl:text>
                    <span class="func_param_name" title="Arugment name"><xsl:value-of select="doc:parameter"/></span>
                </xsl:for-each>
                <xsl:if test="doc:void">
                    <span class="func_param_void" title="No arguments expected">void</span>
                </xsl:if>
                )
        </tt></dd>
    </xsl:template>

    <xsl:template match="doc:cmdsynopsis">
        <div class="command_synopsis">
            <dt>location:</dt>
            <dd><tt class="filename"><xsl:value-of select="@label"/></tt></dd>
            <dt>usage:</dt>
            <dd><tt class="command">
                    <span class="cmd_name" title="Command name"><xsl:value-of select="doc:command"/></span>
                    <xsl:apply-templates select="doc:arg"/>
            </tt></dd>
        </div>
    </xsl:template>

    <xsl:template match="doc:arg">
        <xsl:text> </xsl:text>
        <span class="cmd_arg" title="Argument"><xsl:choose>
                <xsl:when test="@choice = 'req'">{</xsl:when>
                <xsl:otherwise>[</xsl:otherwise>
                </xsl:choose><xsl:apply-templates/><xsl:if test="@rep='repeat'"><span class="cmd_arg_repeat" title="This argument repeats">...</span></xsl:if><xsl:choose>
                <xsl:when test="@choice = 'req'">}</xsl:when>
                <xsl:otherwise>]</xsl:otherwise>
        </xsl:choose></span>
    </xsl:template>

    <xsl:template match="doc:cmdsynopsis[@role='ant']">
        <xsl:variable name="target-name1" select="substring-after(@xlink:href,'#')"/>
        <xsl:variable name="target-name"><xsl:choose>
                <xsl:when test="string-length($target-name1) = 0 and contains(@xlink:href,'#') and doc:command[@role='ant']"><xsl:value-of select="doc:command[@role='ant']"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$target-name1"/></xsl:otherwise>
        </xsl:choose></xsl:variable>
        <xsl:variable name="target" select="document(@xlink:href)//target[@name=$target-name]"/>

        <div class="target_synopsis">
            <xsl:if test="$target/@description">
                <dt>description:</dt>
                <dd><xsl:value-of select="$target/@description"/></dd>
            </xsl:if>

            <xsl:if test="$target/@depends">
                <dt>dependencies:</dt>
                <dd><ol>
                        <xsl:call-template name="ant-dependencies">
                            <xsl:with-param name="depends" select="$target/@depends"/>
                        </xsl:call-template>
                </ol></dd>
            </xsl:if>
        </div>
    </xsl:template>
    <xsl:template name="ant-dependencies">
        <xsl:param name="depends"/>

        <xsl:choose>
            <xsl:when test="contains($depends,',')">
                <xsl:variable name="before" select="normalize-space(substring-before($depends,','))"/>
                <xsl:variable name="after" select="normalize-space(substring-after($depends,','))"/>

                <xsl:if test="string-length($before) &gt; 0">
                    <li><tt class="command role_ant"><a href="/doc/{$module}/ant/{$before}.xml"><xsl:value-of select="$before"/></a></tt></li>
                </xsl:if>

                <xsl:if test="string-length($after) &gt; 0">
                    <xsl:call-template name="ant-dependencies">
                        <xsl:with-param name="depends" select="$after"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:when>
            <xsl:when test="string-length(normalize-space($depends)) &gt; 0">
                <li><tt class="command role_ant"><a href="/doc/{$module}/ant/{$depends}.xml"><xsl:value-of select="$depends"/></a></tt></li>
            </xsl:when>
        </xsl:choose>
    </xsl:template>



    <xsl:template match="*[@xlink:show='embed']">
        <iframe src="{@xlink:href}">
            <p class="redirect">Continue to <a href="{@xlink:href}"><xsl:value-of select="@xlink:href"/></a></p>
        </iframe>
    </xsl:template>

    <xsl:template match="@*"/>
    <xsl:template match="@xlink:href">
        <xsl:choose>
            <xsl:when test="../@xlink:show = 'embed'">
                <iframe src="{@xlink:href}">
                    <p class="redirect">Continue to <a href="{.}"><xsl:value-of select="."/></a></p>
                </iframe>
            </xsl:when>
            <xsl:when test="../@xlink:show = 'replace'">
                <xsl:choose>
                    <xsl:when test="../@xlink:role = 'ant_target' and contains(.,'#')">
                        <xsl:variable name="target" select="substring-after(.,'#')"/>
                        <xsl:call-template name="xml-copy">
                            <xsl:with-param name="node" select="document(.)//target[@name=$target]"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="../@xlink:role = 'xsl_template' and contains(.,'#')">
                        <xsl:variable name="template" select="substring-after(.,'#')"/>
                        <xsl:call-template name="xml-copy">
                            <xsl:with-param name="node" select="document(.)//xsl:template[@name=$template]"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="../@xlink:role = 'js_function' and contains(.,'#')">
                        <xsl:call-template name="js-function">
                            <xsl:with-param name="source" select="iovar:get(.)"/>
                            <xsl:with-param name="name" select="substring-after(.,'#')"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="iovar:get(.)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

