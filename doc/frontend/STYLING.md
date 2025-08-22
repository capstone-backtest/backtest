# 스타일링 전략

이 문서는 프론트엔드 애플리케이션에서 사용되는 스타일링 전략을 설명합니다. 이 프로젝트는 유연하고 일관된 스타일링 솔루션을 제공하기 위해 Bootstrap과 Tailwind CSS를 함께 사용합니다.

## Bootstrap

[Bootstrap](https://getbootstrap.com/)은 주요 UI 프레임워크로 사용됩니다. 이는 미리 빌드된 컴포넌트 세트, 반응형 그리드 시스템 및 일관된 디자인 언어를 제공합니다.

### 통합

Bootstrap은 모든 Bootstrap 컴포넌트에 대한 React 컴포넌트를 제공하는 `react-bootstrap` 라이브러리를 통해 프로젝트에 통합됩니다.

주요 Bootstrap CSS 파일은 `src/main.tsx`에서 가져옵니다:

```tsx
import 'bootstrap/dist/css/bootstrap.min.css';
```

## Tailwind CSS

[Tailwind CSS](https://tailwindcss.com/)는 유틸리티 우선 스타일링에 사용됩니다. 이는 사용자 정의 CSS를 작성하지 않고도 사용자 정의 디자인을 구축하는 데 사용할 수 있는 저수준 유틸리티 클래스 세트를 제공합니다.

### 통합

Tailwind CSS는 PostCSS를 통해 프로젝트에 통합됩니다. 구성은 `tailwind.config.js` 및 `postcss.config.js` 파일에 있습니다.

### 사용법

Tailwind CSS 클래스는 특정 스타일을 적용하기 위해 컴포넌트의 JSX에서 직접 사용됩니다.

```tsx
<div className="p-4 bg-light rounded">
  <h1 className="text-2xl font-bold">Hello, Tailwind!</h1>
</div>
```

## Bootstrap과 Tailwind CSS 결합

Bootstrap은 전반적인 레이아웃과 버튼, 폼, 모달과 같은 미리 빌드된 컴포넌트에 사용됩니다. Tailwind CSS는 개별 요소의 스타일링에 대한 세밀한 제어에 사용됩니다.

이 조합은 Bootstrap의 빠른 개발과 Tailwind CSS의 유연성이라는 두 가지 장점을 모두 제공합니다.

## 사용자 정의 스타일

사용자 정의 CSS는 최소한으로 유지됩니다. 사용자 정의 스타일이 필요한 경우 `src/index.css` 파일에 추가해야 합니다.
