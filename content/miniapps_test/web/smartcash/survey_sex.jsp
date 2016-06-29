<?xml version="1.0" encoding="UTF-8"?>
<%@page language="java" contentType="text/xml; charset=UTF-8"%>

<page version="2.0" style="post_ussd">
  <div>
    <select navigationId="submit" name="sex" title="Choose your sex">
      <option value="female" accesskey="1"><div protocol="telegram">👩</div>Female
      </option>
      <option value="male" accesskey="2"><div protocol="telegram">👨</div>Male
      </option>
    </select>
  </div>
  <navigation id="submit">
    <link accesskey="1" pageId="_ctrl_survey_sex.jsp">Ok</link>
  </navigation>
</page>
