{ This procedure reads in files in Catras format. }
procedure input_catras;
begin
   { first, go to 45th position to get number of rings }
   for x := 1 to 45 do begin
      read(f,junkCh);
      get_possible_error;
      if not data_OK then exit;
      Ch1 := junkCh
   end;

   num_obs := ord(junkCh);
   read(f,junkCh);
   num_obs := (ord(junkCh)*256)+num_obs;

   { go to 55th position to get year }
   for x := 47 to 54 do read(f,junkCh);
   read (f,junkCh);
   firstyr := ord(junkCh);
   read(f,junkCh);
   firstyr := (ord(junkCh)*256)+firstyr;
   year := firstyr;

   { now go to 129th position to read ring measurements }
   for x := 57 to 128 do begin
      read(f,junkCh);
      get_possible_error;
      if not data_OK then exit;
      Ch1 := junkCh
   end;

   { now read num_obs rings beginning with #129! }
   for x := 1 to num_obs do begin
      read(f,junkCh);
      data[year] := ord(junkCh);
      read(f,junkCh);
      dr := ord(junkCh);
      data[year] := data[year]+(dr*256);
      if data[year] = 999 then data[year] := 998;
      inc(year);
   end;
   lastyr := year - 1;
   get_sitecodes
end;
