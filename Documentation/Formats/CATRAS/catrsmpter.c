int catrasfile2(struct dfiledata *tree,HWND hWnd,int impcat)
{
   int i,catbyte[128],byte1,catlength,padby,extras,tempstore;
   char pfetext[_MAX_PATH],line1[33],line2[13],line3[5];
   signed char byte2;
   BOOL reduced = FALSE,lowflag = FALSE, flag9999start = FALSE, flag9999end = FALSE;

   fread(line1,32,1,fp);
   line1[32]=0;

   strcpy_s(stripstring,_countof(stripstring),line1);
   stripspaces();
   strcpy_s(tree->title,_countof(tree->title),stripstring);
 OemToAnsi(tree->title,tree->title);

  wsprintf(pfetext,"%-32s ",tree->title);
   sendtopfe(hWnd,pfetext);

   fread(line2,12,1,fp);
   line2[12]=0;

   for (i=44;i<84;i++)
   	catbyte[i]=fgetc(fp);

   fread(line3,4,1,fp);
   line3[4]=0;

   strcpy_s(stripstring,_countof(stripstring),line3);
   stripspaces();
   strcpy_s(line3,_countof(line3),stripstring);

   for (i=88;i<128;i++)
   	catbyte[i]=fgetc(fp);

   if ( catbyte[45] > 0)
	tree->length = ( catbyte[45]*256)+  catbyte[44];
   else
	tree->length =  catbyte[44];

  wsprintf(pfetext,"%-4d ",tree->length);
   sendtopfe(hWnd,pfetext);

   if ( catbyte[47] > 0)
   {
	tree->sap_chron = ( catbyte[47]*256)+  catbyte[46];
	tree->bark_filt = 'N';
   }
   else
   {
	tree->sap_chron =  catbyte[46];
	tree->bark_filt = 'N';
   }

  wsprintf(pfetext,"%-2d ",tree->sap_chron);
   sendtopfe(hWnd,pfetext);

   if ( catbyte[55] != 0)
	tree->start_date = ( ((signed char)catbyte[55])*256)+  catbyte[54];
   else
	tree->start_date =  catbyte[54];

   if (tree->start_date == 0)
	tree->date_type = 'R';
   else if (tree->start_date > 0)
   {
	tree->start_date += 10000;
	tree->date_type = 'A';
   }
   else
   {
	tree->start_date += 10001;
	tree->date_type = 'A';
   }

  wsprintf(pfetext,"%-5d ",tree->start_date);
   sendtopfe(hWnd,pfetext);

   if (catbyte[58] > 0)
	wsprintf(tree->itrdbspec,"C%d",catbyte[58]);
   if (catbyte[52] == 1 || catbyte[52] == 3 || catbyte[52] == 5)
   	tree->pith = 'C';
   if (catbyte[52] >= 2 && catbyte[52] <= 5)
   	tree->bark_filt = 'Y';

   if (catbyte[65] == 0)
 	wsprintf(tree->author,"CATRAS %s %d.%d.%d ",line3,catbyte[60],catbyte[61],catbyte[62]+1900);
   else
	wsprintf(tree->author,"CATRAS %s %d.%d.%d amend %d.%d.%d",line3,catbyte[60],catbyte[61],catbyte[62]+1900,catbyte[63],catbyte[64],catbyte[65]+1900);

  wsprintf(pfetext,"%-36s ",tree->author);
   sendtopfe(hWnd,pfetext);

   for (i=1;i<=tree->length;i++)
   {
	byte1=fgetc(fp);
	byte2=fgetc(fp);

   	if ( byte2 != 0)
		tree->widths[i] = ( byte2*256)+ byte1;
   	else
		tree->widths[i] =  byte1;
   }

   catlength=tree->length;
   padby=0;
   if (tree->widths[1] == -9999)
   {
   	for (i=2;i<=tree->length;i++)
		tree->widths[i-1] = tree->widths[i];
	padby=1;
	tree->length--;
	tree->start_date++;
	reduced=TRUE;
	flag9999start=TRUE;
   }
   if (tree->widths[1] <= 0)
   {
	for (i=2;i<=tree->length;i++)
		tree->widths[i-1] = tree->widths[i];
	tree->start_date++;
	tree->length--;
        padby++;
	reduced=TRUE;
	strcpy_s(tree->begin,_countof(tree->begin),"H1");
   }
   if (tree->widths[tree->length] == -9999)
   {
	tree->length--;
	reduced=TRUE;
	flag9999end=TRUE;
	if (tree->sap_chron >= 1)
        	tree->sap_chron--;
   }
   if (tree->widths[tree->length] <= 0)
   {
	tree->length--;
	reduced=TRUE;
	if (tree->sap_chron >= 1)
        {
		tree->sap_chron--;
                if (tree->bark_filt == 'N')
			strcpy_s(tree->end,_countof(tree->end),"S1");
		else
		{
			strcpy_s(tree->end,_countof(tree->end),"Y1");
			tree->bark_filt = 'N';
		}
	}
	else
        	strcpy_s(tree->end,_countof(tree->end),"H1");
   }

   if (catbyte[83] == 2)
   {
	tree->trd_type = 'W';

	extras = catlength % 64;
	extras = 64-extras;
	for (i=1;i<=extras;i++)
	{
		byte1=fgetc(fp);
        byte2=fgetc(fp);
	}
   	for (i=1;i<=catlength;i++)
   	{
		byte1=fgetc(fp);
		byte2=fgetc(fp);

   		if ( byte2 > 0)
			tree->hist[i] = ( byte2*256)+ byte1;
   		else
			tree->hist[i] =  byte1;
   	}
	for (i=1;i<=extras;i++)
	{
		byte1=fgetc(fp);
        byte2=fgetc(fp);
	}
   	for (i=1;i<=catlength;i++)
   	{
		byte1=fgetc(fp);
		byte2=fgetc(fp);

   		if ( byte2 > 0)
			tree->up[i] = ( byte2*256)+ byte1;
   		else
			tree->up[i] =  byte1;
   	}
	for (i=1;i<=extras;i++)
	{
		byte1=fgetc(fp);
        byte2=fgetc(fp);
	}
   	for (i=1;i<=catlength;i++)
   	{
		byte1=fgetc(fp);
		byte2=fgetc(fp);

   		if ( byte2 > 0)
			tree->down[i] = ( byte2*256)+ byte1;
   		else
			tree->down[i] =  byte1;
   	}
	if (padby == 1)
	{
		for (i=2;i<=catlength;i++)
        	{
		tree->hist[i-1] = tree->hist[i];
		tree->up[i-1] = tree->up[i];
		tree->down[i-1] = tree->down[i];
        	}
	}
	if (padby == 2)
	{
		for (i=3;i<=catlength;i++)
        	{
		tree->hist[i-2] = tree->hist[i];
		tree->up[i-2] = tree->up[i];
		tree->down[i-2] = tree->down[i];
        	}
	}
	for (i=2;i<=tree->length;i++)
        {
		if (tree->widths[i] > tree->widths[i-1])
                {
			// its ok
		}
		else if (tree->widths[i] < tree->widths[i-1])
		{
			// swap round
			tempstore = tree->up[i];
			tree->up[i] = tree->down[i];
                        tree->down[i] = tempstore;
		}
		else
		{
			// its level and catras format has a problem
			if (tree->up[i] == tree->hist[i])
			{
			  // num level = num hist
			  tree->up[i] = 0;
                          tree->down[i] = 0;
			}
			else
			{
			  // num level < num hist
			  tempstore = tree->hist[i]-tree->up[i];
			  if ((tempstore % 2) == 0)
			  {
				tree->up[i] = tempstore / 2;
				tree->down[i] = tempstore / 2;
			  }
			  else
			  {
				tree->up[i] = tempstore / 2;
				tree->down[i] = tempstore / 2;
				tree->down[i]++;
			  }

                        }
                }

        }

   }
   if (reduced)
   {
	wsprintf(pfetext,"REDUCED %d %d ",tree->length,tree->start_date);
	sendtopfe(hWnd,pfetext);
   }
   if (flag9999start)
   {
	wsprintf(pfetext,"-9999 at start");
	sendtopfe(hWnd,pfetext);
   }
   if (flag9999end)
   {
	wsprintf(pfetext,"-9999 at end");
	sendtopfe(hWnd,pfetext);
   }

   lowflag = FALSE;
   for (i=1;i<=tree->length;i++)
   {
	if (tree->widths[i] < 1)
		lowflag = TRUE;
   }
   if (lowflag == TRUE)
   {
   wsprintf(pfetext,GetResStr(IMPORTZERO));
   sendtopfe(hWnd,pfetext);
   }

   newlines(hWnd,1);

   if (impcat == IMPORT)
	save_all_file(tree,hWnd);
   return(TRUE);
}


