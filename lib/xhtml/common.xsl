<?xml version="1.0"?>
<!--
     Copyright (C) 2018-2019 Virgo Venture, Inc.
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
        doctype-public="-//W3C//DTD XHTML 1.0 Frameset//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"
        omit-xml-declaration="no"
        />

    <xsl:import href="/lib/common.xsl"/>

    <xsl:attribute-set name="html"/>
    <xsl:attribute-set name="body"/>

    <xsl:variable name="title" select="'Web Platform | IOVAR'"/>
    <xsl:variable name="title_url"/>
    <xsl:variable name="desc" select="'Web Development Platform and Shell... a Unix-Like Operating System with Java Foundation'"/>

    <xsl:param name="userxmlstr"/>
    <xsl:variable name="user" select="document(concat('data:text/xml,', $userxmlstr))/me.idfree.lib.User"/>

    <xsl:param name="webappxmlstr"/>
    <xsl:variable name="webapp" select="document(concat('data:text/xml,', $webappxmlstr))/webapp"/>

    <xsl:template match="/">
        <xsl:comment>

            Copyright (C) 2011-2019 Virgo Venture, Inc.
            @%@~LICENSE~@%@

        </xsl:comment>
        <html xsl:use-attribute-sets="html" prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# article: http://ogp.me/ns/article#">
            <head profile="http://microformats.org/profile/rel-license">
                <meta property="og:type" content="article"/>
                <meta property="fb:app_id" content="258824437902116"/>

                <title><xsl:value-of select="$title"/> | iovar</title>
                <meta property="og:site_name" content="IOVAR Web"/>
                <meta name="application-name" content="IOVAR Web"/>
                <meta name="msapplication-tooltip" content="{normalize-space($desc)}"/>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <meta property="og:title" content="{$title} | iovar"/>

                <meta name="description" content="{normalize-space($desc)}"/>
                <meta property="og:description" content="{normalize-space($desc)}"/>

                <link rel="shortcut icon" sizes="16x16 24x24 32x32 48x48 64x64" href="/usr/include/iovar/favicon/favicon.ico"/>
                <link rel="icon" sizes="228x228" href="/usr/include/iovar/favicon/favicon-228.png"/>
                <link rel="icon" sizes="196x196" href="/usr/include/iovar/favicon/favicon-196.png"/>
                <link rel="icon" sizes="195x195" href="/usr/include/iovar/favicon/favicon-195.png"/>
                <link rel="icon" sizes="192x192" href="/usr/include/iovar/favicon/favicon-192.png"/>
                <link rel="icon" sizes="180x180" href="/usr/include/iovar/favicon/favicon-180.png"/>
                <link rel="icon" sizes="152x152" href="/usr/include/iovar/favicon/favicon-152.png"/>
                <link rel="icon" sizes="144x144" href="/usr/include/iovar/favicon/favicon-144.png"/>
                <link rel="icon" sizes="128x128" href="/usr/include/iovar/favicon/favicon-128.png"/>
                <link rel="icon" sizes="120x120" href="/usr/include/iovar/favicon/favicon-120.png"/>
                <link rel="icon" sizes="114x114" href="/usr/include/iovar/favicon/favicon-114.png"/>
                <link rel="icon" sizes="96x96" href="/usr/include/iovar/favicon/favicon-96.png"/>
                <link rel="icon" sizes="76x76" href="/usr/include/iovar/favicon/favicon-76.png"/>
                <link rel="icon" sizes="72x72" href="/usr/include/iovar/favicon/favicon-72.png"/>
                <link rel="icon" sizes="64x64" href="/usr/include/iovar/favicon/favicon-64.png"/>
                <link rel="icon" sizes="57x57" href="/usr/include/iovar/favicon/favicon-57.png"/>
                <link rel="icon" sizes="48x48" href="/usr/include/iovar/favicon/favicon-48.png"/>
                <link rel="icon" sizes="32x32" href="/usr/include/iovar/favicon/favicon-32.png"/>
                <link rel="icon" sizes="24x24" href="/usr/include/iovar/favicon/favicon-24.png"/>
                <link rel="icon" sizes="16x16" href="/usr/include/iovar/favicon/favicon-16.png"/>
                <link rel="apple-touch-icon-precomposed" sizes="180x180" href="/usr/include/iovar/favicon/favicon-180.png"/>
                <link rel="apple-touch-icon-precomposed" sizes="152x152" href="/usr/include/iovar/favicon/favicon-152.png"/>
                <link rel="apple-touch-icon-precomposed" sizes="144x144" href="/usr/include/iovar/favicon/favicon-144.png"/>
                <link rel="apple-touch-icon-precomposed" sizes="120x120" href="/usr/include/iovar/favicon/favicon-120.png"/>
                <link rel="apple-touch-icon-precomposed" sizes="114x114" href="/usr/include/iovar/favicon/favicon-114.png"/>
                <link rel="apple-touch-icon-precomposed" sizes="76x76" href="/usr/include/iovar/favicon/favicon-76.png"/>
                <link rel="apple-touch-icon-precomposed" sizes="72x72" href="/usr/include/iovar/favicon/favicon-72.png"/>
                <link rel="apple-touch-icon-precomposed" sizes="60x60" href="/usr/include/iovar/favicon/favicon-60.png"/>
                <link rel="apple-touch-icon-precomposed" sizes="57x57" href="/usr/include/iovar/favicon/favicon-57.png"/>
                <link rel="apple-touch-icon-precomposed" href="/usr/include/iovar/favicon/favicon-57.png"/>
                <meta name="msapplication-TileColor" content="#FFFFFF"/>
                <meta name="msapplication-TileImage" content="/usr/include/iovar/favicon/favicon-144.png"/>
                <meta name="msapplication-config" content="/usr/include/iovar/favicon/ieconfig.xml"/>
                <meta name="msapplication-square70x70logo" content="/usr/include/iovar/favicon/favicon-70.png"/>
                <meta name="msapplication-square150x150logo" content="/usr/include/iovar/favicon/favicon-150.png"/>
                <meta name="msapplication-wide310x150logo" content="/usr/include/iovar/favicon/favicon-310x150.png"/>
                <meta name="msapplication-square310x310logo" content="/usr/include/iovar/favicon/favicon-310.png"/>

                <link rel="stylesheet" type="text/css" href="/usr/include/iovar/common.css"/>
                <link rel="stylesheet" type="text/css" href="/usr/include/idfree/menu.css"/>

                <!--
                <script type="text/javascript" language="javascript" src="/usr/include/iovar/common.js">/* */</script>
                <script type="text/javascript" language="javascript" src="/usr/include/iovar/ajax.js">/* */</script>
                <script type="text/javascript" language="javascript" src="/usr/include/idfree/menu.js">/* */</script>
                <script type="text/javascript" language="javascript" src="/usr/include/idfree/notice.js">/* */</script>
                -->

                <xsl:call-template name="head"/>

                <meta property="og:image" content="https://iovar.net/usr/include/iovar/favicon/favicon-310x150.png"/>
                <meta property="og:image" content="https://iovar.net/usr/include/iovar/favicon/favicon-512.png"/>
                <meta property="og:image" content="https://iovar.net/usr/include/iovar/logo.png"/>
                <meta property="og:image" content="https://iovar.net/usr/include/iovar/favicon/favicon-64.png"/>

                <!--
                <link rel="mask-icon" href="/usr/include/iovar/favicon/icon.svg" color="#FF0000"/>
                -->
                <link rel="sitemap" type="application/xml" href="/doc/iovar/sitemap" title="Sitemap"/>
                <link rel="copyright" href="http://virgoventure.com/home/for/iovar" title="Copyright (C) Virgo Venture, Inc."/>
                <link rel="license" href="/LICENSE" title="MIT License"/>

                <!-- Global site tag (gtag.js) - Google Analytics -->
                <script async="async" src="https://www.googletagmanager.com/gtag/js?id=UA-36092231-23">/* */</script>
                <script>
                    window.dataLayer = window.dataLayer || [];
                    function gtag(){dataLayer.push(arguments);}
                    gtag('js', new Date());

                    gtag('config', 'UA-36092231-23');
                </script>
            </head>

            <!--body xsl:use-attribute-sets="body" onload="ajaxLoadAll(document.getElementsByClassName('ajaxload'), ajaxMenuReady)"-->
            <body xsl:use-attribute-sets="body">

                <header>
                    <h1><a id="apphome" href="/app" title="Home"><img src="/usr/include/iovar/favicon/favicon-48.png" alt="iovar logo"/></a><xsl:text> </xsl:text>
                        <xsl:choose>
                            <xsl:when test="$title_url = ''">
                                <xsl:value-of select="$title"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <a href="{$title_url}"><xsl:value-of select="$title"/></a>
                            </xsl:otherwise>
                        </xsl:choose>

                        <xsl:if test="$user/id != ''">
                            <xsl:call-template name="authbody"/>
                        </xsl:if>
                    </h1>
                    <xsl:call-template name="header"/>
                </header>

                <hr class="legacy"/>

                <xsl:call-template name="body"/>

                <hr class="legacy"/>

                <footer id="footer">
					<xsl:call-template name="footer"/>

                    <!--xsl:if test="$user/access[string='shell']"-->
                        <a class="easyaccess" target="iovar" href="/$" title="Shell">$</a>
                    <!--/xsl:if-->

                    <!--img src="/usr/include/iovar/footer.png"/><br/-->
                    <center id="copyright">
                        Copyright (C) 2018-2019
                        <a title="Freshly Enterprising" href="http://virgoventure.com/home/for/iovar">Virgo Venture, Inc.</a>
                    </center>
                </footer>
            </body>
        </html>
    </xsl:template>



    <xsl:template match="/error">
        <h2>Yikes!</h2>

        <p class="error">Something went wrong:
            <br/><code><xsl:value-of select="."/></code>
        </p>
    </xsl:template>

    <xsl:template match="/coming-soon">
        <h2>Coming Soon!</h2>

        <p>This area currently under construction. Please check back later.</p>
    </xsl:template>

    <xsl:template match="/access-denied">
        <h2>Access Denied</h2>

        <p>Sorry, you do not have permission for that. If you believe this is an error, please contact the administrator.</p>
    </xsl:template>



    <!-- other sheets can override these templates
         to add their own stuff -->

    <xsl:template name="head">
        <!-- no-op: override this template to supplement the html <head> section -->
    </xsl:template>

    <xsl:template name="header">
        <!-- no-op: override this template to supplement the header area at the top of the page -->
    </xsl:template>

    <xsl:template name="authbody">
        <a id="user" class="easyaccess" href="/auth" title="User Settings">
            <ul class="menu">
                <li><a href="/app/user" title="User and Password Information">My Profile <code><xsl:value-of select="$user/handle"/></code></a></li>
                <li><a class="ajaxload" href="/auth/menu?align=right&amp;token={$user/token}">Webapps</a></li>
                <li><a href="/auth/out" title="End Current Session">Sign Out</a></li>
            </ul>
        </a>
    </xsl:template>

    <xsl:template name="body">
        <main id="body">
            <xsl:apply-templates/>
        </main>
    </xsl:template>

	<xsl:template name="footer">
        <!-- no-op: override this template to supplement the footer area at the bottom of the page -->
	</xsl:template>


</xsl:stylesheet>


