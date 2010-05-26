/* ********************************************************************* */
/*																		 */
/*	Routine de traitement d'un fichier besancon 						 */
/*	Auteur: Rémi Brageu													 */
/*  (c) C2RMF 2009														 */
/*																		 */
/* ********************************************************************* */


        private void traiterBeusac(string NomOrig)
        /* ******************************************************* */
        /* Routine de traitement d'un fichier a la norme Besancon. */
        /* Routine processing of a file has the standard Besancon  */
        /* ******************************************************* */
        {
            StreamReader sr = null;
            StreamWriter sw = null;
            string Ligne;
            string LigneEcrite;
            string[] Ln_Array = null;
            string[] Info_temp = null;
            string[] EnsVal = new string[20001];
            bool FlagTraite = false;
            string NomFicXML;
            string NomXML;
            int nbVal;
            int numval;
            string Date_Orig;
            string Date_Fin;

/* ******************************************************** */
/* 						Tags du fichiers 					*/
/* 							Tags 							*/
/* ******************************************************** */
            string[] s1 = { "ESP" };
            string[] s2 = { "MOE" };
            string[] s3 = { "CAM" };
            string[] s4 = { "LON" };
            string[] s5 = { "POS" };
            string[] s6 = { "ORI" };
            string[] s7 = { "TER" };
            string[] s8 = { "AUB" };
            string[] s9 = { "ECO" };
            int Mem_LastNonEmpty = 0;
            int LastNonEmpty;

            try
            {

                numval = 0;
                // On cree le chemin du fichier XML a la même place
                Info_temp = NomOrig.Split('.');
                NomFichierXML = Info_temp[0] + ".xml";
                // comm il existe pas on va le creer
                NomXML = System.IO.Path.GetFileName(NomFichierXML);
                if (File.Exists(NomFichierXML))
                {
                    File.Delete(NomFichierXML);

                }
                sw = new StreamWriter(NomFichierXML);
                LigneEcrite = "<" + NomXML + ">";
                sw.WriteLine(LigneEcrite);
                // Création d'une instance de StreamReader pour permettre la lecture de notre fichier
                StreamReader monStreamReader = new StreamReader(NomOrig);
                Ligne = monStreamReader.ReadLine();
                while (Ligne != null)
                {
                    FlagTraite = false;
                    //----------------- ----------------------------------------------------
                    //On analyse ce qu'on a dans ligne
                    // We analyze what we have in line
                    //----------------- ----------------------------------------------------
                    if (((Ligne.Contains("date")) & (!FlagTraite)))
                    {
                        //on supprime les blancs avant
                        // it removes the white first before significance
                        Ligne = Ligne.TrimStart(' ');
                        Info_temp = Ligne.Split(':');
                        LigneEcrite = "<DATE> " + Info_temp[2] + " </DATE>";
                        sw.WriteLine(LigneEcrite);
                        InfoCalculs[0] = System.Convert.ToInt16(Info_temp[2]);
                        FlagTraite = true;
                    }
                    if (((Ligne.Contains(".")) & (!FlagTraite)))
                    {
                        //on decoupe et on vire le .
                        Info_temp = Ligne.Split('.');
                        Ligne = Info_temp[1];
                        // on vire les blancs devant
                        Ligne = Ligne.TrimStart(' ');
                        //on découpe selon les -
                        Info_temp = Ligne.Split('-');
                        Vi.Text = Info_temp[0];
                        Bi.Text = Info_temp[1];
                        MCote.Text = Info_temp[2];
                        TAis.Text = Info_temp[3];
                        LigneEcrite = "<VILLE>" + Info_temp[0] + "</VILLE>";
                        sw.WriteLine(LigneEcrite);
                        LigneEcrite = "<BIBLIOTHEQUE>" + Info_temp[1] + "</BIBLIOTHEQUE>";
                        sw.WriteLine(LigneEcrite);
                        LigneEcrite = "<MssCote>" + Info_temp[2] + "</MssCote>";
                        sw.WriteLine(LigneEcrite);
                        LigneEcrite = "<TAis>" + Info_temp[3] + "</TAis>";
                        sw.WriteLine(LigneEcrite);
                        FlagTraite = true;
                        Vi.Text = Info_temp[0];
                        Bi.Text = Info_temp[1];
                        MCote.Text = Info_temp[2];
                        TAis.Text = Info_temp[3];
                    }
                    if (((Ligne.Contains("ESP")) & (!FlagTraite)))
                    {
                        Info_temp = Ligne.Split(s1, StringSplitOptions.None);
                        // on vire les blancs devant
                        Info_temp[1] = Info_temp[1].TrimStart(' ');
                        LigneEcrite = "<SPECIES> " + Info_temp[1] + " </SPECIES>";
                        sw.WriteLine(LigneEcrite);
                        Esp.Text = Info_temp[1];
                        FlagTraite = true;
                        switch (Info_temp[1])
                        {
                            case "QU": InfoCalculs[1] = 1;
                                break;
                            case "FGSY": InfoCalculs[1] = 2;
                                break;
                        }
                    }
                    if (((Ligne.Contains("MOE")) & (!FlagTraite)))
                    {
                        //MOE existe donc on a de la moelle donc on met 1 ds fichier et ds Bdd
                        //MOE is therefore one has spinal So we put a ds and ds file Bdd
                        LigneEcrite = "<PITH> 1 </PITH>";
                        sw.WriteLine(LigneEcrite);
                        Moe.Text = "MOE";
                        FlagTraite = true;
                        InfoCalculs[3] = 1;
                    }

                    if (((Ligne.Contains("CAM")) & (!FlagTraite)))
                    {
                        //CAM existe donc on a de la moelle donc on met 1 ds fichier et ds Bdd
                        //CAM is therefore one has spinal So we put a ds and ds file Bdd
                        LigneEcrite = "<CAMBIUM> 1 </CAMBIUM>";
                        sw.WriteLine(LigneEcrite);
                        Camb.Text = "CAM";
                        FlagTraite = true;
                        InfoCalculs[5] = 1;
                    }

                    if (((Ligne.Contains("LON")) & (!FlagTraite)))
                    {
                        Info_temp = Ligne.Split(s4, StringSplitOptions.None);
                        Info_temp[1] = Info_temp[1].TrimStart(' ');
                        LigneEcrite = "<LENGTH>" + Info_temp[1] + "</LENGTH>";
                        sw.WriteLine(LigneEcrite);
                        LOngu.Text = Info_temp[1];
                        NbValeurs = System.Convert.ToInt16(Info_temp[1]);
                        //Nb de ligne du bloc
                        NbLigneBloc = (NbValeurs / 10) + 1;
                        FlagTraite = true;
                        InfoCalculs[2] = NbValeurs;
                    }
                    if (((Ligne.Contains("POS")) & (!FlagTraite)))
                    {
                        Info_temp = Ligne.Split(s5, StringSplitOptions.None);
                        Info_temp[1] = Info_temp[1].TrimStart(' ');
                        LigneEcrite = "<POSITION>" + Info_temp[1] + "</POSITION>";
                        sw.WriteLine(LigneEcrite);
                        Posi.Text = Info_temp[1];
                        FlagTraite = true;
                        InfoCalculs[9] = System.Convert.ToInt16(Info_temp[1]);
                    }
                    if (((Ligne.Contains("ORI")) & (!FlagTraite)))
                    {
                        Info_temp = Ligne.Split(s6, StringSplitOptions.None);
                        Info_temp[1] = Info_temp[1].TrimStart(' ');
                        /* on a CHIFFRE_ORI + TER + Chiffre_TER */
                        Date_Orig = Info_temp[1];
                        Info_temp = Date_Orig.Split(s7, StringSplitOptions.None);
                        Date_Orig = Info_temp[0].TrimStart(' ');
                        Date_Fin = Info_temp[1].TrimStart(' ');
                        LigneEcrite = "<BEGIN>" + Date_Orig + "</BEGIN>";
                        sw.WriteLine(LigneEcrite);
                        LigneEcrite = "<END>" + Date_Fin + "</END>";
                        sw.WriteLine(LigneEcrite);
                        Orig.Text = Date_Orig;
                        Term.Text = Date_Fin;
                        FlagTraite = true;
                        InfoCalculs[7] = System.Convert.ToInt16(Date_Orig);
                        InfoCalculs[8] = System.Convert.ToInt16(Date_Fin);
                        OTQ.Visible = true;
                    }
                    if (((Ligne.Contains("AUB")) & (!FlagTraite)))
                    {
                        Info_temp = Ligne.Split(s8, StringSplitOptions.None);
                        Info_temp[1] = Info_temp[1].TrimStart(' ');
                        LigneEcrite = "<SAPWOOD>" + Info_temp[1] + "</SAPWOOD>";
                        sw.WriteLine(LigneEcrite);
                        Aub.Text = Info_temp[1];
                        FlagTraite = true;
                        InfoCalculs[6] = System.Convert.ToInt16(Info_temp[1]);
                    }
                    if (((Ligne.Contains("ECO")) & (!FlagTraite)))
                    {
                        //ECO existe donc on a de la moelle donc on met 1 ds fichier et ds Bdd
                        LigneEcrite = "<BARK> 1 </BARK>";
                        sw.WriteLine(LigneEcrite);
                        Ecor.Text = Info_temp[1];
                        FlagTraite = true;
                        InfoCalculs[4] = 1;
                    }
                    //------------------------------------------------------------------------------
                    //On traite les données
                    // we processing datas
                    //------------------------------------------------------------------------------
                    if ((Ligne.Contains("VAL")) || (Ligne.Contains("Valeurs naturelles")))
                    {
                        //----------------- ----------------------------------------------------
                        // On tecrit comme il faut dans le ficher
                        //----------------- ----------------------------------------------------
                        LigneEcrite = "<DONNEES>";
                        sw.WriteLine(LigneEcrite);
                        Mem_LastNonEmpty = 0;
                        // 'on est dans le bon coin
                        //'on decoupe le tableau de valeurs de facon idoine
                        //NbLigneBloc = (nbVal / 10) + 1;
                        for (int i = 1; i <= NbLigneBloc; i++)
                        {

                            Ligne = monStreamReader.ReadLine();
                            // On remplace les , et ; indésirables
                            
                            Ligne = Ligne.Replace(",", "-1");
                            Ligne = Ligne.TrimStart(' ');
                            Ligne = Ligne.Replace(";", " ");
                            Ligne = Ligne.TrimStart(' ');
                            Info_temp = Ligne.Split(' ');
                 
                            //initialisation du compteur de donnÃƒÂ©es
                            LastNonEmpty = -1;
                            //on vire les blancs inutiles
                            for (int j = 0; j <= Info_temp.Length - 1; j++)
                            {
                                if (!string.IsNullOrEmpty(Info_temp[j]))
                                {
                                    LastNonEmpty += 1;
                                    Info_temp[LastNonEmpty] = Info_temp[j];

                                }
                            }

                            //On a ecrit la première ligne
                            for (int j = 0; j <= LastNonEmpty; j++)
                            {
                                EnsVal[j + Mem_LastNonEmpty] = Info_temp[j];
                                TableauValeur[numval] = System.Convert.ToInt16(Info_temp[j]);
                                numval++;
                                TxTBxVal.Text += Info_temp[j] + "\r\n";
                                LigneEcrite = "<VALUES>" + Info_temp[j] + "</VALUES>";
                                sw.WriteLine(LigneEcrite);
                                Console.WriteLine(TableauValeur[numval]);
                            }
                            Mem_LastNonEmpty += LastNonEmpty + 1;

                            sw.WriteLine(LigneEcrite);
                        }
                        LigneEcrite = "</DONNEES>";
                        sw.WriteLine(LigneEcrite);
                    }
                    Ligne = monStreamReader.ReadLine();

                }
                LigneEcrite = "</" + NomXML + ">";
                sw.WriteLine(LigneEcrite);
                sw.Close();
                monStreamReader.Close();
            }
            catch
            {
            }
        }