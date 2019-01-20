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

    <xsl:template match="/*">
        <html>
            <head>
                <title>Thompson Aircraft Repair - New <xsl:value-of select="local-name()"/></title>
                <meta name="viewport" content="width=device-width" />
                <link rel="stylesheet" type="text/css" href="/usr/include/general.css"/>
            </head>

            <body>
                <h1>Thompson Aircraft Repair</h1>

                <ul id="user">
                    <li><a href="/home/index.shtml">Logout</a></li>
                    <li><a href="/$/usr/bin/cart/ls">View Cart</a></li>
                </ul>

                <img id="header" src="/usr/include/header.png"/>

                <ul id="social">
                    <li><a target="facebook" href="http://facebook.com/ThompsonAircraftRepair" title="Visit us on Facebook"><img src="/usr/include/facebook.png"/></a></li>
                    <li><a target="google" href="https://plus.google.com/118174123128101777292" title="Visit us on Google+"><img src="/usr/include/google.png"/></a></li>
                    <li><a target="twitter" href="http://twitter.com/Tarpinc14" title="Visit us on Twitter"><img src="/usr/include/twitter.png"/></a></li>
                    <li><a target="tumblr" href="http://tarpinc.tumblr.com" title="Visit us on Tumblr"><img src="/usr/include/tumblr.png"/></a></li>
                    <li><a target="linkedin" href="http://www.linkedin.com/profile/view?id=392341885" title="Visit us on LinkedIn"><img src="/usr/include/linkedin.png"/></a></li>
                </ul>

                <ul id="menu">
                    <li><a href="/home/index.shtml">Home</a></li>
                    <li><a href="/home/about.shtml">About</a></li>
                    <li><a href="/$/usr/bin/shopping/ls">Parts for Sale</a></li>
                    <li><a href="/home/repairs.shtml">Repair Services</a></li>
                    <li><a href="/home/contact.shtml">Contact</a></li>
                    <li class="separator"/>
                    <li><a class="current" href="/$/index">Dashboard</a></li>
                </ul>

                <div id="body">
                    <h2>New <xsl:value-of select="local-name()"/></h2>

                    <form method="POST" action="/$/bin/{local-name()}/mk">
                        <ul>
                            <xsl:apply-templates/>
                        </ul>

                        <input type="submit" value="Create"/>
                        <input type="reset" onclick="window.history.back()" value="Cancel"/>
                    </form>

                    <sub id="footer">
                        <p><img src="/usr/include/rapidssl.gif"/></p>
                        <img src="/usr/include/footer.jpg"/><br/>
                        Copyright (C) 2014-2015 Thompson Aircraft Repair, LLC<br/>
                        Design &amp; Hosting by <a target="lannocc" href="http://lannocc.com" title="Web Sites. Business IT. Computer Service. Located in Billings, Montana.">Lannocc Technologies</a>
                    </sub>
                </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="id"/>

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

