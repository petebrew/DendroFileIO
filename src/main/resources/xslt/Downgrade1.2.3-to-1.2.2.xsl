<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Transforms a v1.2.3 TRiDaS XML file to a v1.2.2 TRiDaS XML file -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tridasold="http://www.tridas.org/1.2.3"
	xmlns:t="http://www.tridas.org/1.2.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<!-- Rename elevation tag to altitude -->
	<xsl:template match="tridasold:elevation">
	  <xsl:element name="t:altitude">
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
		<xsl:attribute name="xsi:schemaLocation">http://www.tridas.org/1.2.2 http://www.tridas.org/1.2.2/tridas-1.2.2.xsd</xsl:attribute>
	</xsl:template>



</xsl:stylesheet>