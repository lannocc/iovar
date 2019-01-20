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

    <xsl:import href="/lib/ui/data/xhtml/common.xsl"/>

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
                    <title>Edit <xxx:value-of select="local-name()"/></title>
                    <link rel="stylesheet" type="text/css" href="/usr/include/edit.css"/>
                    <script language="javascript" type="text/javascript" src="/usr/include/iovar/ui/general.js">/* */</script>
                    <script language="javascript" type="text/javascript" src="/usr/include/iovar/ui/edit.js">/* */</script>
                </head>

                <body>
                    <h1>Edit <xxx:value-of select="local-name()"/></h1>

                    <xxx:choose>
                        <xxx:when test="deleted = 'Y'">
                            <p>Cannot edit this <xxx:value-of select="local-name()"/> because it has been deleted.</p>
                        </xxx:when>
                        <xxx:otherwise>
                            <div class="edit">
                                <form method="POST" target="async" name="{{local-name()}}" onsubmit="return !submits.push (this)" action="/$/bin/data/{{local-name()}}/mod?={{id}}">
                                    <xxx:choose>
                                        <xxx:when test="/item">
                                            <a class="button" target="item_label" href="/$/bin/item/labels?=item&amp;={{id}}">Print Tag</a>
                                        </xxx:when>
                                        <xxx:when test="/part">
                                            <a class="button" target="part_labels" href="/$/bin/item/labels?=part&amp;={{id}}">Print Tag(s)</a>
                                        </xxx:when>
                                        <xxx:when test="/customer">
                                            <a class="button" target="customer_labels" href="/$/bin/item/labels?=customer&amp;={{id}}">Print Tag(s)</a>
                                        </xxx:when>
                                    </xxx:choose>

                                    <xxx:if test="/item or /part">
                                        <fieldset class="other">
                                            <legend>
                                                <xxx:choose>
                                                    <xxx:when test="/part">representative image</xxx:when>
                                                    <xxx:otherwise>images</xxx:otherwise>
                                                </xxx:choose>
                                            </legend>

                                            <iframe class="image" src="/$/bin/data/{{local-name()}}/image?={{id}}">Image would be here if your browser supported frames.</iframe>
                                        </fieldset>
                                    </xxx:if>

                                    <ul class="fields">
                                        <xxx:apply-templates/>
                                    </ul>

                                    <noscript class="buttons">
                                        <input type="submit" class="submit" value="Modify"/>
                                        <input type="reset" class="cancel" value="Cancel"/>
                                    </noscript>
                                </form>

                                <xxx:choose>
                                    <xxx:when test="local-name() = 'customer'">
                                        <xxx:call-template name="addresses"/>

                                        <fieldset class="other">
                                            <legend>items</legend>
                                            <xxx:if test="customer-items">
                                                <xxx:apply-templates select="customer-items/*"/>
                                            </xxx:if>
                                        </fieldset>
                                    </xxx:when>
                                    <xxx:when test="local-name() = 'part'">
                                        <fieldset class="other">
                                            <legend>items</legend>
                                            <xxx:if test="part-items">
                                                <xxx:apply-templates select="part-items/*"/>
                                            </xxx:if>
                                            <a class="create" href="/$/bin/item/new?={{id}}" onclick="return !loadInPopup(this)">new item</a>
                                        </fieldset>
                                    </xxx:when>
                                </xxx:choose>

                                <div class="buttons">
                                    <span id="auto_save" class="submit"/>
                                    <script language="javascript" type="text/javascript">
                                        var async = get ('async');
                                        if (async)
                                        {
                                            get ('auto_save').innerHTML = 'Your changes are saved automatically';
                                        }
                                        else
                                        {
                                            var form = document.forms['<xxx:value-of select="local-name()"/>'];

                                            var buttons = document.createElement ('div');
                                            buttons.className = 'buttons';

                                            var submit = document.createElement ('input');
                                            submit.type = 'submit';
                                            submit.className = 'submit';
                                            submit.value = 'Modify';
                                            buttons.appendChild (submit);

                                            var cancel = document.createElement ('input');
                                            cancel.type = 'reset';
                                            cancel.className = 'cancel';
                                            cancel.value = 'Cancel';
                                            buttons.appendChild (cancel);

                                            form.appendChild (buttons);
                                        }
                                    </script>

                                    <a class="delete" href="/$/bin/data/{{local-name()}}/rm?={{id}}" onclick="return confirmDelete (this)">delete</a>
                                </div>
                            </div>
                        </xxx:otherwise>
                    </xxx:choose>
                </body>
            </html>
        </xxx:template>

        <xxx:template match="item/id">
            <li>
                <span class="label">Item Number:</span><xxx:text> </xxx:text>
                <i><xxx:value-of select="."/></i>
            </li>
        </xxx:template>

        <xxx:template match="id|deleted|reg_address|mail_address">
            <input type="hidden" name="{{local-name()}}" value="{{.}}"/>
        </xxx:template>

        <xxx:template match="customer/source">
            <li>
                <label><span class="label">Source:</span><xxx:text> </xxx:text><xxx:value-of select="Name"/></label>
            </li>
        </xxx:template>

        <xxx:template match="registration|mailing|customer-items|part-items|part/Image"/>

        <xxx:template match="Notes|Description">
            <li>
                <label><span class="label"><xxx:value-of select="local-name()"/>:</span><xxx:text> </xxx:text><xxx:text disable-output-escaping="yes">&lt;textarea name="</xxx:text><xxx:value-of select="local-name()"/><xxx:text disable-output-escaping="yes">" onchange="submits.push (this.form)"&gt;</xxx:text><xxx:value-of select="."/><xxx:text disable-output-escaping="yes">&lt;/textarea&gt;</xxx:text></label>
            </li>
        </xxx:template>

        <xxx:template match="image-list">
            <div>
                Images go here

                <xxx:apply-templates/>
            </div>
        </xxx:template>

        <xxx:template match="image">
            <img src="/pub/images/{{id}}" style="width:100px"/>
        </xxx:template>

        <xxx:template match="customer-list/customer">
            <option value="{{id}}">
                <xxx:if test="../@id=id"><xxx:attribute name="selected">selected</xxx:attribute></xxx:if>
                <xxx:if test="deleted = 'Y'"><xxx:attribute name="class">deleted</xxx:attribute>--deleted--<xxx:text> </xxx:text></xxx:if>
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
        </xxx:template>

        <xxx:template match="part-list/part">
            <option value="{{id}}">
                <xxx:if test="../@id=id"><xxx:attribute name="selected">selected</xxx:attribute></xxx:if>
                <xxx:if test="deleted = 'Y'"><xxx:attribute name="class">deleted</xxx:attribute>--deleted--<xxx:text> </xxx:text></xxx:if>
                <xxx:value-of select="Mfg-Part-Num"/><xxx:text> </xxx:text>
                <xxx:if test="Description != ''">
                    - <xxx:value-of select="Description"/>
                </xxx:if>
            </option>
        </xxx:template>


        <xxx:template name="addresses">
            <form id="reg_address" name="reg_address" target="async" class="address" onsubmit="return !submits.push (this)" action="/$/bin/customer/saveaddress" method="POST">
                <fieldset class="fields">
                    <legend>Registration Address</legend>
                    <xxx:apply-templates select="registration/address"/>
                </fieldset>

                <noscript class="buttons">
                    <input type="submit" class="submit" value="Modify"/>
                    <input type="reset" class="cancel" value="Cancel"/>
                </noscript>
                <script language="javascript" type="text/javascript">
                    var async = get ('async');
                    if (! async)
                    {
                        var form = document.forms['reg_address'];

                        var buttons = document.createElement ('div');
                        buttons.className = 'buttons';

                        var submit = document.createElement ('input');
                        submit.type = 'submit';
                        submit.className = 'submit';
                        submit.value = 'Modify';
                        buttons.appendChild (submit);

                        var cancel = document.createElement ('input');
                        cancel.type = 'reset';
                        cancel.className = 'cancel';
                        cancel.value = 'Cancel';
                        buttons.appendChild (cancel);

                        form.appendChild (buttons);
                    }
                </script>
            </form>

            <form id="mail_address" name="mail_address" target="async" class="address" onsubmit="return !submits.push (this)" action="/$/bin/customer/saveaddress" method="POST">
                <fieldset class="fields">
                    <legend>Mailing Address
                        <label style="font-size:smaller">
                            <input name="same" type="checkbox" onchange="chksame(this)">
                                <xxx:if test="mailing/same">
                                    <xxx:attribute name="checked">checked</xxx:attribute>
                                </xxx:if>
                            </input>
                            same as registration
                        </label>
                    </legend>
                    <xxx:apply-templates select="mailing/address"/>
                </fieldset>

                <noscript class="buttons">
                    <input type="submit" class="submit" value="Modify"/>
                    <input type="reset" class="cancel" value="Cancel"/>
                </noscript>
                <script language="javascript" type="text/javascript">
                    var async = get ('async');
                    if (! async)
                    {
                        var form = document.forms['mail_address'];

                        var buttons = document.createElement ('div');
                        buttons.className = 'buttons';

                        var submit = document.createElement ('input');
                        submit.type = 'submit';
                        submit.className = 'submit';
                        submit.value = 'Modify';
                        buttons.appendChild (submit);

                        var cancel = document.createElement ('input');
                        cancel.type = 'reset';
                        cancel.className = 'cancel';
                        cancel.value = 'Cancel';
                        buttons.appendChild (cancel);

                        form.appendChild (buttons);
                    }
                </script>
            </form>
        </xxx:template>

        <xxx:template match="address">
            <input type="hidden" name="customer" value="{{../../id}}"/>
            <input type="hidden" name="type" value="{{local-name(..)}}"/>
            <xxx:if test="not(../same)"><input type="hidden" name="id" value="{{id}}"/></xxx:if>

            <ul>
                <li><xxx:text disable-output-escaping="yes">&lt;textarea name="Address" class="address" onchange="submits.push(this.form)"</xxx:text><xxx:if test="../same"><xxx:text> </xxx:text> disabled="disabled"</xxx:if><xxx:text disable-output-escaping="yes">&gt;</xxx:text><xxx:value-of select="Address"/><xxx:text disable-output-escaping="yes">&lt;/textarea&gt;</xxx:text></li>
                <li>
                    <label>City: <input name="City" type="text" class="city" value="{{City}}" onchange="submits.push(this.form)"><xxx:if test="../same"><xxx:attribute name="disabled">disabled</xxx:attribute></xxx:if></input></label><xxx:text> </xxx:text>
                    <label>State: <input name="State" type="text" class="state" value="{{State}}" onchange="submits.push(this.form)"><xxx:if test="../same"><xxx:attribute name="disabled">disabled</xxx:attribute></xxx:if></input></label><xxx:text> </xxx:text>
                    <label>Zip: <input name="Zip" type="text" class="zip" value="{{Zip}}" onchange="submits.push(this.form)"><xxx:if test="../same"><xxx:attribute name="disabled">disabled</xxx:attribute></xxx:if></input></label>
                </li>

                <!--
                <div class="buttons">
                    <input type="submit" value="Save"/>
                    <input type="reset" value="Cancel"/>
                </div>
                -->
            </ul>
        </xxx:template>

        <xxx:template match="part/Mystery">
            <li>
                <label><span class="label"><xxx:value-of select="local-name()"/>:</span><xxx:text> </xxx:text>
                    <input type="checkbox" name="{{local-name()}}" value="Y" onchange="submits.push (this.form)">
                        <xxx:if test=". = 'Y'"><xxx:attribute name="checked">checked</xxx:attribute></xxx:if>
                    </input>
                </label>
            </li>
        </xxx:template>

        <xxx:template match="customer-items/item-list|part-items/item-list">
            <ul>
                <xxx:apply-templates/>
            </ul>
        </xxx:template>

        <xxx:template match="customer-items/item-list/item">
            <li>
                <a target="item" href="/$/bin/item/view?={{id}}" onclick="return !loadInPopup (this)">
                    <xxx:value-of select="part/manufacturer/Name"/><xxx:text> </xxx:text>
                    <xxx:value-of select="part/Mfg-Part-Num"/><xxx:text> </xxx:text>
                    <xxx:value-of select="part/Mfg-Model-Num"/><xxx:text> </xxx:text>
                    <xxx:value-of select="part/Description"/><xxx:text> </xxx:text>
                    <xxx:if test="Serial-Num != ''">#<xxx:value-of select="Serial-Num"/></xxx:if>
                </a>
            </li>
        </xxx:template>

        <xxx:template match="part-items/item-list/item">
            <li>
                <a target="item" href="/$/bin/item/view?={{id}}" onclick="return !loadInPopup (this)">
                    <xxx:if test="customer/Company != ''">
                        <xxx:value-of select="customer/Company"/>
                        <xxx:if test="customer/Last-Name != '' or customer/First-Name != ''">:<xxx:text> </xxx:text></xxx:if>
                    </xxx:if>
                    <xxx:if test="customer/Last-Name != ''">
                        <xxx:value-of select="customer/Last-Name"/>
                        <xxx:if test="customer/First-Name != ''">,<xxx:text> </xxx:text></xxx:if>
                    </xxx:if>
                    <xxx:value-of select="customer/First-Name"/>
                    <xxx:if test="Aircraft-Model != ''"><xxx:text> - </xxx:text><xxx:value-of select="Aircraft-Model"/></xxx:if>
                    <xxx:if test="Engine-Model != ''"><xxx:text> - </xxx:text><xxx:value-of select="Engine-Model"/></xxx:if>
                    <xxx:if test="Serial-Num != ''"><xxx:text> - #</xxx:text><xxx:value-of select="Serial-Num"/></xxx:if>
                </a>
            </li>
        </xxx:template>




        <!-- *-list -->
        <xxx:template match="*[substring (local-name(), string-length(local-name())-4)='-list']" priority="-1">
            <li>
                <label><span class="label">
                        <xxx:value-of select="translate (substring (local-name(), 1, 1), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xxx:value-of select="substring (local-name(), 2, string-length (local-name()) - 6)"/>:
                    </span>
                    <xxx:text> </xxx:text>
                    <select name="{{substring (local-name(), 1, string-length (local-name()) - 5)}}" onchange="submits.push (this.form)">
                        <option value="">--none--</option>
                        <xxx:apply-templates/>
                    </select>
                </label>
            </li>
        </xxx:template>

        <!-- *-list/* (record) -->
        <xxx:template match="*[substring (local-name(), string-length(local-name())-4)='-list']/*" priority="-1">
            <option value="{{id}}">
                <xxx:if test="../@id=id"><xxx:attribute name="selected">selected</xxx:attribute></xxx:if>
                <xxx:if test="deleted = 'Y'"><xxx:attribute name="class">deleted</xxx:attribute>--deleted--<xxx:text> </xxx:text></xxx:if>
                <xxx:if test="Code">
                    <xxx:value-of select="Code"/>:<xxx:text> </xxx:text>
                </xxx:if>
                <xxx:value-of select="Name"/>
            </option>
        </xxx:template>



        <xxx:template match="*" priority="-5">
            <li>
                <label><span class="label"><xxx:value-of select="@tag-name-orig"/>:</span><xxx:text> </xxx:text>
                    <input type="text" name="{{@tag-name-orig}}" value="{{.}}" onchange="submits.push (this.form)"/>
                </label>
            </li>
        </xxx:template>

    </xsl:template>

</xsl:stylesheet>

