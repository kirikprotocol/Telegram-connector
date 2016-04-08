<?xml version="1.0" encoding="KOI8-R"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:meta="http://whoisd.eyelinecom.com/sads/meta" exclude-result-prefixes="meta">

  <xsl:template match="br">
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="b">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="strong">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="i">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="em">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="code">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="pre">
    <xsl:copy>
      <xsl:value-of select="."/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="p">
    <xsl:apply-templates/>
    <xsl:text>
    </xsl:text>
  </xsl:template>

  <xsl:template match="a">
    <xsl:text> </xsl:text>
    <xsl:if test="string-length(text()) > 0">
      <xsl:value-of select='text()'/><xsl:text>: </xsl:text>
    </xsl:if>
    <xsl:value-of select='@href'/><xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="u">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="attachment" mode="attachment">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="meta:phone">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="link" mode="command">
    <xsl:variable name="formId" select="//*/@navigationId"/>

    <xsl:variable name="totalRows" select="count(//navigation)"/>
    <xsl:variable name="row" select="count(../preceding-sibling::navigation) + 1"/>

    <xsl:if
        test="count(//*[@navigationId])=0 or count(parent::navigation[@id])=0 or count(parent::navigation[@id])!=0 and parent::navigation/@id!=$formId ">
      <xsl:if test="string-length(text()) > 0">
        <button href="{@pageId}">

          <!-- Add a row number in case of multiple `navigation' blocks. -->
          <xsl:if test="$totalRows > 1">
            <xsl:attribute name="row">
              <xsl:value-of select="$row"/>
            </xsl:attribute>
          </xsl:if>

          <xsl:if test="count(child::div)>0">
            <xsl:apply-templates select="div" mode="icon"/>
            <xsl:text> </xsl:text>
          </xsl:if>
          <xsl:value-of select='text()'/>
        </button>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="div">
    <xsl:if
        test="string-length(normalize-space(text())) > 0 or count(child::div)>0 or count(child::a)>0 or count(child::input)>0 or count(child::select)>0">

      <xsl:apply-templates/>

      <xsl:if test="count(child::node()[position()=last() and name='br'])=0 or count(child::br)=0">
        <xsl:text>&#xa;</xsl:text>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="div" mode="icon">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="input">
    <xsl:if test="count(@type)=0 or @type != 'hidden'">
      <xsl:value-of select="@title"/><xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="input" mode="command">
    <xsl:if test="count(@type)=0 or @type!='hidden'">
      <xsl:variable name="formId" select="@navigationId"/>
      <xsl:variable name="actionId" select="/page/navigation[@id=$formId]/link/@pageId"/>
      <xsl:variable name="formPageId">
        <xsl:value-of select='$actionId'/>
        <xsl:for-each select="//input[@type='hidden' and @navigationId=$formId]">
          <xsl:choose>
            <xsl:when test="contains($actionId,'?') or position() > 1">
              <xsl:value-of select='concat("&amp;",@name,"=",@value)'/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select='concat("?",@name,"=",@value)'/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>
      <xsl:variable name="paramName" select="@name"/>
      <input>
        <xsl:attribute name="href">
          <xsl:value-of select='$formPageId'/>
        </xsl:attribute>
        <xsl:attribute name="name">
          <xsl:value-of select='$paramName'/>
        </xsl:attribute>
      </input>
    </xsl:if>
  </xsl:template>


  <xsl:template match="select">
    <xsl:value-of select="@title"/><xsl:text>:</xsl:text><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="select" mode="command">
    <xsl:variable name="formId" select="@navigationId"/>
    <xsl:variable name="actionId" select="/page/navigation[@id=$formId]/link/@pageId"/>
    <xsl:variable name="formPageId">
      <xsl:value-of select='$actionId'/>
      <xsl:for-each select="//input[@type='hidden' and @navigationId=$formId]">
        <xsl:choose>
          <xsl:when test="contains($actionId,'?') or position() > 1">
            <xsl:value-of select='concat("&amp;",@name,"=",@value)'/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select='concat("?",@name,"=",@value)'/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="paramName" select="@name"/>
    <xsl:for-each select="option">
      <button>
        <xsl:attribute name="href">
          <xsl:choose>
            <xsl:when test="contains($formPageId,'?')">
              <xsl:value-of select='concat($formPageId,"&amp;",$paramName,"=",@value)'/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select='concat($formPageId,"?",$paramName,"=",@value)'/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:if test="string-length(text()) > 0">
          <xsl:if test="count(child::div)>0">
            <xsl:apply-templates select="div" mode="icon"/>
            <xsl:text> </xsl:text>
          </xsl:if>
          <xsl:value-of select='text()'/>
        </xsl:if>
      </button>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="title">
    <xsl:if test="string-length(text()) > 0">
      <xsl:apply-templates/>
      <xsl:text>:</xsl:text><xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:variable name="escaped" select="normalize-space(concat('.', ., '.'))"/>
    <xsl:variable name="normalized" select="substring($escaped, 2, string-length($escaped) - 2)"/>

    <xsl:call-template name="replace">
      <xsl:with-param name="string">
        <xsl:value-of select="$normalized"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="/">
    <page>
      <message>
        <xsl:apply-templates select="/page/title"/>
        <xsl:apply-templates select="/page/div[(count(@type)=0 or @type!='sms')]"/>
        <!-- xsl:apply-templates select="//input"/ -->
      </message>
      <xsl:if test="count(/page/div[@type='sms'])!=0">
        <message>
          <xsl:apply-templates select="/page/div[@type='sms']"/>
        </message>
      </xsl:if>
      <xsl:apply-templates select="//select" mode="command"/>
      <xsl:apply-templates select="//input" mode="command"/>
      <xsl:apply-templates select="/page/navigation/link" mode="command"/>
      <xsl:apply-templates select="//attachment" mode="attachment"/>
    </page>
  </xsl:template>

  <xsl:template name="replace">
    <xsl:param name="string"/>
    <xsl:choose>
      <xsl:when test="contains($string, ' &#10;')">
        <xsl:value-of select="substring-before($string, ' &#10;')"/>
        <xsl:call-template name="replace">
          <xsl:with-param name="string" select="substring-after($string, '&#10;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>


