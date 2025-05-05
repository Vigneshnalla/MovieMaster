package com.vigverse.stack.dto;

import lombok.Builder;

@Builder
public record MailBody(String from, String to, String subject, String text) {}

