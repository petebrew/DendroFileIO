<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Transforms a v1.2.2 TRiDaS XML file to a v1.2.3 TRiDaS XML file -->


<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:old="http://www.tridas.org/1.2.2">
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

   <!-- replace xsi:schemaLocation attribute -->
   <xsl:template match="@xsi:schemaLocation">
      <xsl:attribute name="xsi:schemaLocation">http://www.tridas.org/1.2.3 tridas.xsd</xsl:attribute>
   </xsl:template>

	<!-- Rename altitude tag to elevation -->
	<xsl:template match="altitude">
		<elevation>
			<xsl:apply-templates select="@*|node()" />
		</elevation>
	</xsl:template>

</xsl:stylesheet>