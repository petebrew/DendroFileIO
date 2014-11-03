<?xml version="1.0"?>

<!-- Transforms a v1.2.2 TRiDaS XML file to a v1.2.3 TRiDaS XML file -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tridasold="http://www.tridas.org/1.2.2"
	xmlns:t="http://www.tridas.org/1.2.3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    
    <xsl:output omit-xml-declaration="yes" indent="yes"/>
    
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<!-- Rename altitude tag to elevation -->
	<xsl:template match="tridasold:altitude">
	  <xsl:element name="t:elevation">
	    <xsl:apply-templates/>
	  </xsl:element>
	</xsl:template>

	<xsl:template match="@tridasold:*">
		<xsl:attribute name="t:{local-name()}">
            <xsl:value-of select="." />
        </xsl:attribute>
	</xsl:template>

	<xsl:template match="tridasold:*">
		<xsl:element name="t:{local-name()}">
			<xsl:apply-templates select="node()|@*" />
		</xsl:element>
	</xsl:template>

	<!-- replace xsi:schemaLocation attribute -->
	<xsl:template match="@xsi:schemaLocation">
		<xsl:attribute name="xsi:schemaLocation">http://www.tridas.org/1.2.3 http://www.tridas.org/1.2.3/tridas-1.2.3.xsd</xsl:attribute>
	</xsl:template>



</xsl:stylesheet>